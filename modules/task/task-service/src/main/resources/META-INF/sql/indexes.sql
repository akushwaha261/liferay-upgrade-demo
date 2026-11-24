create index IX_AF8A94C0 on ACME_Task (groupId, status);
create unique index IX_F66FA446 on ACME_Task (uuid_[$COLUMN_LENGTH:75$], groupId);