-- begin DDCRD_DIAGNOSE_EXECUTION_LOG
alter table DDCRD_DIAGNOSE_EXECUTION_LOG add constraint FK_DDCRD_DIAEXELOG_EXERESFIL foreign key (EXECUTION_RESULT_FILE_ID) references SYS_FILE(ID)^
create index IDX_DDCRD_DIAEXELOG_EXERESFIL on DDCRD_DIAGNOSE_EXECUTION_LOG (EXECUTION_RESULT_FILE_ID)^
-- end DDCRD_DIAGNOSE_EXECUTION_LOG
