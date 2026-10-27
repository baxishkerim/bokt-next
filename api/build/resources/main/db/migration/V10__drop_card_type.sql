-- Карты выдачи больше не хранятся в пуле организации: карта клиента задаётся в заявке.
-- Удаляем карты типа PAYOUT и колонку type (остаётся только карта-источник организации).
delete from org_cards where type = 'PAYOUT';
drop index if exists ix_org_cards_type;
alter table org_cards drop column if exists type;
