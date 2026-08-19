CREATE SCHEMA IF NOT EXISTS catalog;

ALTER TABLE public.product SET SCHEMA catalog;
ALTER TABLE public.product_category SET SCHEMA catalog;
ALTER TABLE public.product_type SET SCHEMA catalog;
ALTER TABLE public.brand SET SCHEMA catalog;
ALTER TABLE public.unit SET SCHEMA catalog;
