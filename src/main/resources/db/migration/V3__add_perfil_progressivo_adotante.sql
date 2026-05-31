alter table adotantes
	add column if not exists tipo_moradia varchar(255),
	add column if not exists espaco_disponivel varchar(255),
	add column if not exists tempo_disponivel varchar(255),
	add column if not exists experiencia_animais varchar(255),
	add column if not exists possui_criancas boolean,
	add column if not exists possui_caes boolean,
	add column if not exists possui_gatos boolean,
	add column if not exists telefone varchar(255),
	add column if not exists cidade varchar(255);

create table if not exists adotante_especies_preferidas (
	adotante_id uuid not null,
	especie varchar(255) not null,
	ordem integer not null,
	constraint fk_adotante_especies_preferidas_adotante foreign key (adotante_id) references adotantes,
	primary key (adotante_id, ordem)
);
