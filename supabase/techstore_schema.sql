create table if not exists public.usuarios (
  id integer generated always as identity primary key,
  nombre text not null,
  email text not null unique,
  password text not null,
  rol text not null default 'usuario',
  created_at timestamptz not null default now()
);

create table if not exists public.productos (
  id integer generated always as identity primary key,
  nombre text not null,
  descripcion text default '',
  precio double precision not null default 0,
  stock integer not null default 0,
  imagen_url text default '',
  created_at timestamptz not null default now()
);

create table if not exists public.carritos (
  id integer generated always as identity primary key,
  usuario_id text not null,
  producto_id text not null,
  nombre_producto text not null,
  cantidad integer not null default 1,
  precio_unitario double precision not null default 0,
  imagen_url text default '',
  created_at timestamptz not null default now()
);

create table if not exists public.ordenes (
  id integer generated always as identity primary key,
  usuario_id text not null,
  cliente_nombre text not null,
  estado text not null default 'pendiente',
  total double precision not null default 0,
  fecha bigint not null,
  created_at timestamptz not null default now()
);

insert into storage.buckets (id, name, public)
values ('productos', 'productos', true)
on conflict (id) do nothing;

alter table public.usuarios enable row level security;
alter table public.productos enable row level security;
alter table public.carritos enable row level security;
alter table public.ordenes enable row level security;

create policy "lectura publica usuarios" on public.usuarios for select to anon, authenticated using (true);
create policy "crear usuarios" on public.usuarios for insert to anon, authenticated with check (true);
create policy "actualizar usuarios" on public.usuarios for update to anon, authenticated using (true) with check (true);
create policy "eliminar usuarios" on public.usuarios for delete to anon, authenticated using (true);

create policy "lectura publica productos" on public.productos for select to anon, authenticated using (true);
create policy "crear productos" on public.productos for insert to anon, authenticated with check (true);
create policy "actualizar productos" on public.productos for update to anon, authenticated using (true) with check (true);
create policy "eliminar productos" on public.productos for delete to anon, authenticated using (true);

create policy "lectura publica carritos" on public.carritos for select to anon, authenticated using (true);
create policy "crear carritos" on public.carritos for insert to anon, authenticated with check (true);
create policy "actualizar carritos" on public.carritos for update to anon, authenticated using (true) with check (true);
create policy "eliminar carritos" on public.carritos for delete to anon, authenticated using (true);

create policy "lectura publica ordenes" on public.ordenes for select to anon, authenticated using (true);
create policy "crear ordenes" on public.ordenes for insert to anon, authenticated with check (true);
create policy "actualizar ordenes" on public.ordenes for update to anon, authenticated using (true) with check (true);
create policy "eliminar ordenes" on public.ordenes for delete to anon, authenticated using (true);

create policy "subir imagenes productos" on storage.objects for insert to anon, authenticated
with check (bucket_id = 'productos');

create policy "leer imagenes productos" on storage.objects for select to anon, authenticated
using (bucket_id = 'productos');
