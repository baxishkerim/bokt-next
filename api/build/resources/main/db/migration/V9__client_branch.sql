-- Филиал клиента: клиенты фильтруются по филиалу так же, как кредиты
-- (модератор/оператор видят только свой филиал; директор — всю организацию).
alter table clients add column branch_id bigint;
create index ix_client_branch on clients(branch_id);
