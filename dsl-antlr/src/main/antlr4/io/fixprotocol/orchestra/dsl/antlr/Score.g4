/** Simple statically-typed programming language with functions and variables
 *  taken from "Language Implementation Patterns" book.
 */
grammar Score;

EQUAL : ('=='|'eq') ;
NOT_EQUAL : ('!='|'ne') ;
LT : ('<'|'lt') ;
LE : ('<='|'le') ;
GE : ('>='|'ge') ;
GT : ('>'|'gt') ;
ADD : '+' ;
SUB : '-' ;
MUL : '*' ;
DIV : '/' ;
MOD : ('%'|'mod') ;
LPAREN : '(' ;
RPAREN : ')' ;
NOT : '!' ;
OR : ('or' | '||') ;
AND : ('and' | '&&') ;
IN : 'in' ;
LBRACE : '{' ;
RBRACE : '}' ;
LBRACK : '[' ;
RBRACK : ']' ;
VAR : '$' ;
USCORE : '_';

anyExpression:
      assignment
    | expr
    ;
assignment: var '=' expr ;

expr:
        '!' expr                			# booleanNot
    |   expr op=('*'|'/'|'%'|'mod') expr  	# mulDiv
    |   expr op=('+'|'-') expr      		# addSub
    |   val=expr 'in' '{' member+=expr (',' member+=expr)* '}'	# contains
    |   val=expr 'between' min=expr 'and' max=expr    # range
    |   expr op=('<'|'<='|'>'|'>='|'lt'|'le'|'gt'|'ge') expr     # relational
    |   expr op=('=='|'!='|'eq'|'ne') expr  # equality
    |   expr op=('and'|'&&') expr   		# logicalAnd
    |   expr op=('or'|'||') expr    		# logicalOr
    |   '(' expr ')'            # parens
    |   INT                     # integer
    |   DECIMAL                 # decimal
    |   CHAR                    # character
    |   STRING                  # string
    |   var                     # variable // lowest priority so variables do not shadow keywords
    ;


index: '[' INT ']' ;

pred: '[' ID '=' expr ']' ;

var: ('$'|'in.'|'out.') ID (index | pred)? ('.' ID (index | pred)? )*;

DECIMAL: SIGN? DIGIT+ '.' DIGIT+ ;

INT: SIGN? DIGIT+;

STRING:	'"' STRINGCHAR+ '"' ;

CHAR:	'\'' STRINGCHAR '\'' ;

ID:   LETTER (LETTER | DIGIT | '_')* ;

fragment
STRINGCHAR:
	    ~["\\]
	|	ESC
	;

fragment
ESC: '\\' [btnfr"'\\] ;

fragment
LETTER: [a-zA-Z] ;

fragment
NONZERO: [1-9] ;

fragment
DIGIT: [0-9] ;

fragment
SIGN:	[+-] ;

WS:   [ \t\n\r]+ -> skip ;

COMMENT:   '/*' .*? '*/' -> skip ;

LINE_COMMENT:   '//' ~[\r\n]* -> skip ;



