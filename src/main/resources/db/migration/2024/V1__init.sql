create table risiko_rapport
(
    id              varchar(36) primary key,
    isOwner         boolean     not null,
    ownerIdent      varchar(8)  not null,
    serviceName     varchar(50) not null,
    opprettet       timestamp default current_timestamp,
    endret          timestamp default current_timestamp
);

