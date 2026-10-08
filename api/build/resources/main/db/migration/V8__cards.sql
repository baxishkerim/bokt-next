-- Карты организации: тип (материнская/выдачи), статус, остаток лимита.
-- Валюта системы только AZN, поэтому currency_id у карты больше не нужен.

alter table org_cards add column type varchar(16) not null default 'MOTHER';
alter table org_cards add column status varchar(16) not null default 'ACTIVE';
alter table org_cards add column balance_minor bigint not null default 0;
alter table org_cards drop column if exists currency_id;

create index ix_org_cards_type on org_cards(type);

-- Карта клиента в заявке больше не обязательна: TOPUP идёт на карту выдачи организации.
alter table credits alter column client_card_pan drop not null;
