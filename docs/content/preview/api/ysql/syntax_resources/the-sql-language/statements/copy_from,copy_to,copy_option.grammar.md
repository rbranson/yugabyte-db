```output.ebnf
copy_from ::= COPY table_name [ ( column_name [ , ... ] ) ]  FROM 
              { 'filename' | PROGRAM 'command' | STDIN }  
              [ [ WITH ] ( copy_option [ , ... ] ) ]

copy_to ::= COPY { table_name [ ( column_names ) ] | subquery }  TO 
            { 'filename' | PROGRAM 'command' | STDOUT }  
            [ [ WITH ] ( copy_option [ , ... ] ) ]

copy_option ::= FORMAT format_name
                | OIDS [ boolean ]
                | FREEZE [ boolean ]
                | DELIMITER 'delimiter_character'
                | NULL 'null_string'
                | HEADER [ boolean ]
                | QUOTE 'quote_character'
                | ESCAPE 'escape_character'
                | FORCE_QUOTE { ( column_names ) | * }
                | FORCE_NOT_NULL ( column_names )
                | FORCE_NULL ( column_names )
                | ENCODING 'encoding_name'
                | ROWS_PER_TRANSACTION int_literal
                | DISABLE_FK_CHECK
                | REPLACE
                | SKIP int_literal
```
