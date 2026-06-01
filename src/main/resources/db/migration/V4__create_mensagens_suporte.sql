create table if not exists mensagens_suporte (
	id uuid primary key,
	adotante_id uuid not null,
	assunto varchar(120) not null,
	mensagem varchar(1000) not null,
	status varchar(30) not null,
	criado_em timestamp not null,
	constraint fk_mensagens_suporte_adotante foreign key (adotante_id) references adotantes(id)
);

create index if not exists idx_mensagens_suporte_adotante on mensagens_suporte(adotante_id);
create index if not exists idx_mensagens_suporte_status on mensagens_suporte(status);
