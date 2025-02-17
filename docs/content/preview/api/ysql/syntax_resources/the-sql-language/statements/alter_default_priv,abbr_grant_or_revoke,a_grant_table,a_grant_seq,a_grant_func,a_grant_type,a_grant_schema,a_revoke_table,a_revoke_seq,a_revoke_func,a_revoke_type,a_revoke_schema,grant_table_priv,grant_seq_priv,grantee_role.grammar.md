```output.ebnf
alter_default_priv ::= ALTER DEFAULT PRIVILEGES 
                       [ FOR { ROLE | USER } role_name [ , ... ] ]  
                       [ IN SCHEMA schema_name [ , ... ] ] 
                       abbr_grant_or_revoke

abbr_grant_or_revoke ::= a_grant_table
                         | a_grant_seq
                         | a_grant_func
                         | a_grant_type
                         | a_grant_schema
                         | a_revoke_table
                         | a_revoke_seq
                         | a_revoke_func
                         | a_revoke_type
                         | a_revoke_schema

a_grant_table ::= GRANT { grant_table_priv [ , ... ]
                          | ALL [ PRIVILEGES ] } ON TABLES TO 
                  grantee_role [ , ... ]  [ WITH GRANT OPTION ]

a_grant_seq ::= GRANT { grant_seq_priv [ , ... ]
                        | ALL [ PRIVILEGES ] } ON SEQUENCES TO 
                grantee_role [ , ... ]  [ WITH GRANT OPTION ]

a_grant_func ::= GRANT { EXECUTE | ALL [ PRIVILEGES ] } ON 
                 { FUNCTIONS | ROUTINES } TO grantee_role [ , ... ]  
                 [ WITH GRANT OPTION ]

a_grant_type ::= GRANT { USAGE | ALL [ PRIVILEGES ] } ON TYPES TO 
                 grantee_role [ , ... ]  [ WITH GRANT OPTION ]

a_grant_schema ::= GRANT { USAGE | CREATE | ALL [ PRIVILEGES ] } ON 
                   SCHEMAS TO grantee_role [ , ... ]  
                   [ WITH GRANT OPTION ]

a_revoke_table ::= REVOKE [ GRANT OPTION FOR ] 
                   { grant_table_priv [ , ... ] | ALL [ PRIVILEGES ] } 
                   ON TABLES  FROM grantee_role [ , ... ] 
                   [ CASCADE | RESTRICT ]

a_revoke_seq ::= REVOKE [ GRANT OPTION FOR ] 
                 { grant_seq_priv [ , ... ] | ALL [ PRIVILEGES ] } ON 
                 SEQUENCES  FROM grantee_role [ , ... ] 
                 [ CASCADE | RESTRICT ]

a_revoke_func ::= REVOKE [ GRANT OPTION FOR ] 
                  { EXECUTE | ALL [ PRIVILEGES ] } ON 
                  { FUNCTIONS | ROUTINES }  FROM grantee_role 
                  [ , ... ] [ CASCADE | RESTRICT ]

a_revoke_type ::= REVOKE [ GRANT OPTION FOR ] 
                  { USAGE | ALL [ PRIVILEGES ] } ON TYPES  FROM 
                  grantee_role [ , ... ] [ CASCADE | RESTRICT ]

a_revoke_schema ::= REVOKE [ GRANT OPTION FOR ] 
                    { USAGE | CREATE | ALL [ PRIVILEGES ] } ON SCHEMAS 
                     FROM grantee_role [ , ... ] 
                    [ CASCADE | RESTRICT ]

grant_table_priv ::= SELECT
                     | INSERT
                     | UPDATE
                     | DELETE
                     | TRUNCATE
                     | REFERENCES
                     | TRIGGER

grant_seq_priv ::= USAGE | SELECT | UPDATE

grantee_role ::= [ GROUP ] role_name
                 | PUBLIC
                 | CURRENT_USER
                 | SESSION_USER
```
