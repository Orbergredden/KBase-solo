--
-- PostgeSQL KBase update
--

--\connect kbase_test

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
--SET client_encoding = 'WIN1251';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = kbase, public, pg_catalog;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.03.00.023', 
       descr = 'new version support of template for documents',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '11.03.2021 19:27', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## move table template_themes #######################################
/*
CREATE SEQUENCE kbase.seq_template_themes
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_template_themes OWNER TO kbase;

do $$ 
<<seq_template_themes>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.template_themes
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_template_themes', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_template_themes $$;
---------------------------------------
CREATE TABLE kbase.template_themes
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_template_themes'::regclass),
    name character varying(25) COLLATE pg_catalog."default",
    descr character varying(25) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_template_themes_id PRIMARY KEY (id)
)
TABLESPACE pg_default;

ALTER TABLE kbase.template_themes OWNER to kbase;
GRANT ALL ON TABLE kbase.template_themes TO kbase;
GRANT SELECT ON TABLE kbase.template_themes TO kbase_user;

COMMENT ON TABLE kbase.template_themes IS 'Темы для показа документов.';
COMMENT ON COLUMN kbase.template_themes.user_created IS 'Тот, кто создал запись';
COMMENT ON COLUMN kbase.template_themes.user_modified IS 'Тот, кто вносил последние изменения в запись';
---------------------------------------
-- копируем информацию
insert into kbase.template_themes
select * from public.template_themes
;
---------------------------------------
-- пересоздаем внешние ключи зависимых таблиц
ALTER TABLE kbase.current_style DROP CONSTRAINT fk_current_style_theme_id;

ALTER TABLE kbase.current_style
   ADD CONSTRAINT fk_current_style_theme_id FOREIGN KEY
          (theme_id)
          REFERENCES kbase.template_themes (id)
             MATCH SIMPLE
             ON DELETE CASCADE
             ON UPDATE CASCADE
          NOT DEFERRABLE INITIALLY IMMEDIATE
;

ALTER TABLE public.template_required_files DROP CONSTRAINT fk_template_required_files_theme_id;

ALTER TABLE public.template_required_files
   ADD CONSTRAINT fk_template_required_files_theme_id FOREIGN KEY
          (theme_id)
          REFERENCES kbase.template_themes (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;

ALTER TABLE public.templates DROP CONSTRAINT fk_templates_theme_id;

ALTER TABLE public.templates
   ADD CONSTRAINT fk_templates_theme_id FOREIGN KEY
          (theme_id)
          REFERENCES kbase.template_themes (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;
------------------------------ 
-- удаляем старые обьекты
DROP TABLE public.template_themes;
DROP SEQUENCE public.seq_template_themes;

--######## create table template #######################################

CREATE SEQUENCE kbase.seq_template
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_template OWNER TO kbase;

do $$ 
<<seq_template>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.templates
	;
	SELECT pg_catalog.setval('kbase.seq_template', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_template $$;

---------------------------------------
CREATE TABLE kbase.template
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_template'::regclass),
	parent_id bigint NULL,
	type INTEGER NOT NULL DEFAULT 0,
	name character varying(25) COLLATE pg_catalog."default",
    descr character varying(50) COLLATE pg_catalog."default",
    body text COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_template_id PRIMARY KEY (id)
)
TABLESPACE pg_default;

ALTER TABLE kbase.template OWNER to kbase;
GRANT ALL ON TABLE kbase.template TO kbase;
GRANT SELECT ON TABLE kbase.template TO kbase_user;

COMMENT ON TABLE kbase.template IS 'Шаблоны документов (их инфоблоков) и шаблоны зарезервированных классов';
COMMENT ON COLUMN kbase.template.type IS '0-шаблон, 1-директория ; (для зарезервированных) 10 - шаблон, 11 - директория';
COMMENT ON COLUMN kbase.template.user_created IS 'Тот, кто создал запись';
COMMENT ON COLUMN kbase.template.user_modified IS 'Тот, кто вносил последние изменения в запись';
---------------------------------------
-- копируем информацию
do $$ 
<<template_migrate>>
declare
-- строим дерево новых шаблонов
	l_i               record;
	l_j               record;
	v_theme_id_new    bigint;
	v_infotype_id_new bigint;
	v_result          bigint;
begin
	CREATE OR REPLACE FUNCTION kbase.temp_add_style(
		p_theme_id bigint, p_infotype_id bigint, p_style_id bigint, p_parent_id bigint) 
		RETURNS bigint AS 
	$temp_add_style$
	declare
		l_s              record;
		l_t              record;
		v_style_id_new   bigint;
		v_result         bigint;
	BEGIN
		for l_s in (select id, parent_id, infotype_id, name, descr, 
					       date_created, date_modified, user_created, user_modified
					  from kbase.infotype_style
					 where parent_id = p_style_id
					   and infotype_id = p_infotype_id
		           )
		loop
			-- add style in new template table
			select nextval('kbase.seq_template') into v_style_id_new;
			insert into kbase.template (id, parent_id, type, name, descr, body, 
										date_created, date_modified, user_created, user_modified)
				values (v_style_id_new, p_parent_id, 1, l_s.name, l_s.descr, '3,'||l_s.id, 
					    l_s.date_created, l_s.date_modified, l_s.user_created, l_s.user_modified)
			;
			-- select and add old template in new template table
			insert into kbase.template (parent_id, name, descr, body, 
										date_created, date_modified, user_created, user_modified)
			select v_style_id_new, name, descr, body, date_created, date_modified, user_created, user_modified
			  from public.templates
			 where theme_id = p_theme_id
			   and infotype_id = p_infotype_id
			   and infotype_style_id = l_s.id
			;
			-- call recursive function
			v_result := kbase.temp_add_style(p_theme_id, p_infotype_id, l_s.id, v_style_id_new);
		end loop;
    	RETURN 0;
	END;
  	$temp_add_style$ language plpgsql;
	----------------------------------------------------------------
	--delete from kbase.template; -- where type = 1;
	
	FOR l_i IN (select id,name,descr,date_created,date_modified,user_created,user_modified 
				  from kbase.template_themes 
				 order by id
			   )
    LOOP
		select nextval('kbase.seq_template') into v_theme_id_new;
		insert into kbase.template (id, type, name, descr, body, date_created, date_modified, user_created, user_modified)
			values (v_theme_id_new, 1, l_i.name, l_i.descr, '1,'||l_i.id, 
				    l_i.date_created, l_i.date_modified, l_i.user_created, l_i.user_modified)	
		;
		for l_j in (select id,name,table_name,descr,date_created,date_modified,user_created,user_modified
					  from public.infotype
					 where id > 0
				   )
		loop
			select nextval('kbase.seq_template') into v_infotype_id_new;
			insert into kbase.template (id, parent_id, type, name, descr, body, 
										date_created, date_modified, user_created, user_modified)
				values (v_infotype_id_new, v_theme_id_new, 1, l_j.name, l_j.descr, '2,'||l_j.id, 
					    l_j.date_created, l_j.date_modified, l_j.user_created, l_j.user_modified)	
			;
			v_result := kbase.temp_add_style(l_i.id, l_j.id, 0, v_infotype_id_new);
		end loop;
	end loop;

	DROP FUNCTION kbase.temp_add_style(bigint,bigint,bigint,bigint);

	raise notice 'v_result =  %', v_result;
end template_migrate $$;

--######## create table template_files #######################################
CREATE SEQUENCE kbase.seq_template_files
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_template_files OWNER TO kbase;

do $$ 
<<seq_template_files>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.template_required_files
	;
	SELECT pg_catalog.setval('kbase.seq_template_files', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_template_files $$;

---------------------------------------------------------
CREATE TABLE kbase.template_files (
    id bigint NOT NULL DEFAULT nextval('kbase.seq_template_files'::regclass),
	parent_id bigint NULL,
    theme_id bigint NOT NULL,
	type integer NOT NULL DEFAULT 0,
    file_type integer NOT NULL DEFAULT 0,
    file_name character varying(25) COLLATE pg_catalog."default",
    descr character varying(25) COLLATE pg_catalog."default",
    body text COLLATE pg_catalog."default",
    body_bin bytea,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_template_files_id PRIMARY KEY (id),
    CONSTRAINT fk_template_files_theme_id FOREIGN KEY (theme_id)
        REFERENCES kbase.template_themes (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
) TABLESPACE pg_default;

ALTER TABLE kbase.template_files OWNER to kbase;
GRANT ALL ON TABLE kbase.template_files TO kbase;
GRANT SELECT ON TABLE kbase.template_files TO kbase_user;

COMMENT ON TABLE kbase.template_files
    IS 'Необходимые файлы для отображения документов';

COMMENT ON COLUMN kbase.template_files.parent_id
    IS 'родительская директория';

COMMENT ON COLUMN kbase.template_files.type
    IS '0-файл, 1-директория ; (для необязательных файлов) 10 - файл, 11 - директория';
	
COMMENT ON COLUMN kbase.template_files.file_type
    IS 'Тип файла : 1 - текстовый ; 2 - картинка ; 3 - бинарный';

COMMENT ON COLUMN kbase.template_files.body_bin
    IS 'содержимое бинарных файлов';

COMMENT ON COLUMN kbase.template_files.user_created
    IS 'Тот, кто создал запись';

COMMENT ON COLUMN kbase.template_files.user_modified
    IS 'Тот, кто вносил последние изменения в запись';

---------------------------------------
-- копируем информацию
insert into kbase.template_files (id, theme_id, type, file_type, file_name, descr, body, body_bin, date_created, date_modified, user_created, user_modified)
select id, theme_id, 0, file_type, file_name, descr, body, body_bin, date_created, date_modified, user_created, user_modified
  from public.template_required_files
;
-- Заменяем NULL на 0
update kbase.template_files
   set parent_id = 0
 where parent_id is null
;

--######## move table infotype #######################################
CREATE TABLE kbase.infotype
(
    id bigint NOT NULL,
    name character varying(25) COLLATE pg_catalog."default",
    table_name character varying(25) COLLATE pg_catalog."default",
    descr character varying(100) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_infotype_id PRIMARY KEY (id)
) TABLESPACE pg_default;

ALTER TABLE kbase.infotype OWNER to kbase;
GRANT ALL ON TABLE kbase.infotype TO kbase;
GRANT SELECT ON TABLE kbase.infotype TO kbase_user;
---------------------------------------
-- копируем информацию
insert into kbase.infotype
select * from public.infotype
;
---------------------------------------
-- пересоздаем внешние ключи зависимых таблиц
ALTER TABLE public.info DROP CONSTRAINT fk_info_infotypeid;

ALTER TABLE public.info
   ADD CONSTRAINT fk_info_infotypeid FOREIGN KEY
          (infotypeid)
          REFERENCES kbase.infotype (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;

ALTER TABLE kbase.infotype_style DROP CONSTRAINT fk_infotype_style_infotype_id;

ALTER TABLE kbase.infotype_style
   ADD CONSTRAINT fk_infotype_style_infotype_id FOREIGN KEY
          (infotype_id)
          REFERENCES kbase.infotype (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;

ALTER TABLE public.templates DROP CONSTRAINT fk_templates_infotype_id;

ALTER TABLE public.templates
   ADD CONSTRAINT fk_templates_infotype_id FOREIGN KEY
          (infotype_id)
          REFERENCES kbase.infotype (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;
------------------------------ 
-- удаляем старые обьекты
DROP TABLE public.infotype;

--######## create table template_style #######################################
CREATE SEQUENCE kbase.seq_template_style
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_template_style OWNER TO kbase;

do $$ 
<<seq_template_style>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from kbase.infotype_style
	;
	SELECT pg_catalog.setval('kbase.seq_template_style', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_template_style $$;

-----------------------------------------------------------------------------------
CREATE TABLE kbase.template_style
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_template_style'::regclass),
    parent_id bigint,
	type bigint NOT NULL,
    infotype_id bigint NOT NULL,
    name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    descr character varying(50) COLLATE pg_catalog."default",
	tag character varying(50) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_template_style_id PRIMARY KEY (id),
    CONSTRAINT fk_template_style_infotype_id FOREIGN KEY (infotype_id)
        REFERENCES kbase.infotype (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
) TABLESPACE pg_default;

ALTER TABLE kbase.template_style OWNER to kbase;
GRANT ALL ON TABLE kbase.template_style TO kbase;
GRANT SELECT ON TABLE kbase.template_style TO kbase_user;

COMMENT ON TABLE kbase.template_style IS 'Стили шаблонов документов';
COMMENT ON COLUMN kbase.template_style.type IS '0-стиль, 1-директория ; (для зарезервированных) 10 - стиль, 11 - директория';
COMMENT ON COLUMN kbase.template_style.tag IS 'уникальный текстовый идентификатор для зарезервированных стилей';

---------------------------------------
-- поправляем информацию в старой таблице
update kbase.infotype_style set parent_id = 0 where parent_id is null;

-- копируем информацию
do $$ 
<<template_style_migrate>>
declare
-- строим дерево новых стилей
	v_style_row       kbase.infotype_style%rowtype;
	l_i               record;
	v_result          bigint;
begin
	CREATE OR REPLACE FUNCTION kbase.temp_add_style(
		p_infotype_id bigint, p_style_id_old bigint, p_parent_id bigint) 
		RETURNS bigint AS 
	$temp_add_style$
	declare
		l_s              record;
		v_style_id_new   bigint;
		v_result         bigint;
	BEGIN
		for l_s in (select id, parent_id, infotype_id, name, descr, 
					       date_created, date_modified, user_created, user_modified
					  from kbase.infotype_style
					 where parent_id = p_style_id_old
					   and infotype_id = p_infotype_id
		           )
		loop
			-- add dir in new table
			select nextval('kbase.seq_template_style') into v_style_id_new;
			insert into kbase.template_style (id, parent_id, type, infotype_id, name, descr, 
									          date_created, date_modified, user_created, user_modified)
				values (v_style_id_new, p_parent_id, 1, p_infotype_id, l_s.name, l_s.descr,
					    l_s.date_created, l_s.date_modified, l_s.user_created, l_s.user_modified)
			;
			-- add old style in new table
			insert into kbase.template_style (id, parent_id, type, infotype_id, name, descr, 
									          date_created, date_modified, user_created, user_modified)
				values (l_s.id, v_style_id_new, 0, p_infotype_id, l_s.name, l_s.descr,
					    l_s.date_created, l_s.date_modified, l_s.user_created, l_s.user_modified)
			;
			-- call recursive function
			v_result := kbase.temp_add_style(p_infotype_id, l_s.id, v_style_id_new);
		end loop;
    	RETURN 0;
	END;
  	$temp_add_style$ language plpgsql;
	--------------------------------------------------------------
	-- delete from kbase.template_style;
	
	-- добавляем псевдо стиль "По умолчанию"
	select *
	  into v_style_row
	  from kbase.infotype_style
	 where id = 0
	;
	insert into kbase.template_style (id, parent_id, type, infotype_id, name, descr, 
									  date_created, date_modified, user_created, user_modified)
		values (v_style_row.id, v_style_row.parent_id, 0, v_style_row.infotype_id, v_style_row.name, v_style_row.descr, 
				v_style_row.date_created, v_style_row.date_modified, v_style_row.user_created, v_style_row.user_modified)
	;
	-- проходимся по инфо типам
	for l_i in (select id from kbase.infotype where id > 0)
	loop
		v_result := kbase.temp_add_style(l_i.id, 0, 0);
	end loop;
	
	DROP FUNCTION kbase.temp_add_style(bigint,bigint,bigint);

	raise notice 'v_result =  %', v_result;	
end template_style_migrate $$;

---------------------------------------
-- пересоздаем внешние ключи зависимых таблиц
ALTER TABLE kbase.current_style DROP CONSTRAINT fk_current_style_infotype_style_id;
--ALTER TABLE kbase.current_style DROP CONSTRAINT fk_current_style_template_style_id;

ALTER TABLE kbase.current_style
   ADD CONSTRAINT fk_current_style_template_style_id FOREIGN KEY
          (infotype_style_id)
          REFERENCES kbase.template_style (id)
             MATCH SIMPLE
             ON DELETE CASCADE
             ON UPDATE CASCADE
          NOT DEFERRABLE INITIALLY IMMEDIATE
;

ALTER TABLE public.info DROP CONSTRAINT fk_info_infotypestyleid;
--ALTER TABLE public.info DROP CONSTRAINT fk_info_template_style_id;

ALTER TABLE public.info
   ADD CONSTRAINT fk_info_template_style_id FOREIGN KEY
          (infotypestyleid)
          REFERENCES kbase.template_style (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;
--######## create table template_style_link #######################################
CREATE SEQUENCE kbase.seq_template_style_link
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_template_style_link OWNER TO kbase;

-----------------------------------------------------------------------------------
CREATE TABLE kbase.template_style_link
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_template_style_link'::regclass),
	style_id bigint NOT NULL,
	theme_id bigint NOT NULL,
	template_id bigint NOT NULL,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_template_style_link_id PRIMARY KEY (id),
	CONSTRAINT k_template_style_link_u1 UNIQUE (style_id, theme_id, template_id),
    CONSTRAINT fk_template_style_link_template_style_id FOREIGN KEY (style_id)
        REFERENCES kbase.template_style (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_template_style_link_template_themes_id FOREIGN KEY (theme_id)
        REFERENCES kbase.template_themes (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_template_style_link_template_id FOREIGN KEY (template_id)
        REFERENCES kbase.template (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
) TABLESPACE pg_default;

ALTER TABLE kbase.template_style_link OWNER to kbase;
GRANT ALL ON TABLE kbase.template_style_link TO kbase;
GRANT SELECT ON TABLE kbase.template_style_link TO kbase_user;

COMMENT ON TABLE kbase.template_style_link IS 'связь стиля с темой и шаблоном';

---------------------------------------
-- поправляем информацию в новой таблице
update kbase.template set parent_id = 0 where parent_id is null;

-- добавляем информацию по связям
do $$ 
<<template_style_link_migrate>>
declare
-- строим связь стиля с темой и шаблоном
	v_result          bigint;
begin
	CREATE OR REPLACE FUNCTION kbase.temp_add_style_link(
		p_theme_id bigint, p_style_id bigint, p_parent_id bigint) 
		RETURNS bigint AS 
	$temp_add_style_link$
	declare
		l_s              record;
		v_theme_id       bigint := p_theme_id;
		v_style_id       bigint := p_style_id;
		v_field_type     bigint;
		v_field_value    bigint;
		v_result         bigint;
	BEGIN
		for l_s in (select id, parent_id, type, body
					  from kbase.template 
					 where parent_id = p_parent_id
				   )
		loop
			if l_s.type = 0 then        -- template
				insert into kbase.template_style_link (theme_id, style_id, template_id)
					values (v_theme_id, v_style_id, l_s.id)
				;
			end if;
			if l_s.type = 1 then        -- directory
				v_field_type  = split_part (l_s.body, ',', 1);
				v_field_value = split_part (l_s.body, ',', 2);
			
				if v_field_type = 1 then
					v_theme_id := v_field_value;
				end if;
				if v_field_type = 3 then
					v_style_id := v_field_value;
				end if;
			
				-- call recursive function
				v_result := kbase.temp_add_style_link(v_theme_id, v_style_id, l_s.id);
			end if;
		end loop;
    	RETURN 0;
	END;
  	$temp_add_style_link$ language plpgsql;
	--------------------------------------------------------------
	-- delete from kbase.template_style_link;
	
	v_result := kbase.temp_add_style_link(null, null, 0);
	
	DROP FUNCTION kbase.temp_add_style_link(bigint,bigint,bigint);

	raise notice 'v_result =  %', v_result;	
end template_style_link_migrate $$;
*/
--######## create function template_file_get_pathname ######################################
CREATE OR REPLACE FUNCTION kbase.template_file_get_pathname(
	p_id bigint,   -- id директорії чи файла для шаблонів
	p_delimiter character varying DEFAULT '/'::character varying,
	p_withFileName integer default 0) -- 0 - шлях з кінцевим іменем файла (директорії); 1 - без
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
-- get file's tree path
DECLARE
	l_i record;
	v_retVal character varying (10000) := '';
	v_isFirst boolean := true;
BEGIN
	FOR l_i IN (WITH RECURSIVE FilePath ( id, parent_id, file_name ) AS 
                (SELECT f1.id, f1.parent_id, f1.file_name 
                   FROM template_files f1
                  WHERE f1.id = p_id
                  UNION 
                 SELECT f2.id, f2.parent_id, f2.file_name 
                   FROM template_files f2
                  INNER JOIN FilePath ON (FilePath.parent_id = f2.id) 
                 ) 
                 select * from FilePath --order by parent_id desc
	           )
	LOOP
		IF v_isFirst THEN
			v_isFirst := false;
			if p_withFileName = 0 then
				v_retVal := l_i.file_name;
			end if;
		ELSE
			v_retVal := l_i.file_name || p_delimiter || v_retVal;
		END IF;
	END LOOP;

    return v_retVal;
END;
$BODY$;

ALTER FUNCTION kbase.template_file_get_pathname(bigint, character varying, integer)
    OWNER TO kbase;

COMMENT ON FUNCTION kbase.template_file_get_pathname(bigint, character varying, integer)
    IS 'get file''s of templates tree path';
--<<