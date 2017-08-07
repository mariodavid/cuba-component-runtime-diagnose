-- begin DDCRD_DIAGNOSE_EXECUTION_LOG
create table DDCRD_DIAGNOSE_EXECUTION_LOG (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    EXECUTION_SUCCESSFUL boolean not null,
    EXECUTION_TIMESTAMP timestamp not null,
    EXECUTION_USER varchar(255),
    EXECUTION_RESULT_FILE_ID uuid,
    DIAGNOSE_TYPE varchar(255) not null,
    EXECUTION_TYPE varchar(255) not null,
    --
    primary key (ID)
)^
-- end DDCRD_DIAGNOSE_EXECUTION_LOG
