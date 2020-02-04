/** 
 * DSL grammar for FIX Orchestra
 */
grammar Score;

EQUAL : ('=='|'eq') ;
NOT_EQUAL : ('!='|'ne') ;
LT : ('<'|'lt') ;
LE : ('<='|'le') ;
GE : ('>='|'ge') ;
GT : ('>'|'gt') ;
ADD : '+' ;
HYPHEN : '-' ;
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
COLON: ':';
DOT: '.';
HASH: '#';
CODE: '^';
EXISTS: 'exists';

anyExpression:
      assignment
    | expr
    ;
assignment: var '=' expr ;

expr:
		'-' expr							# unaryMinus
    |   '!' expr                			# logicalNot
    |   expr op=('*'|'/'|'%'|'mod') expr  	# mulDiv
    |   expr op=('+'|'-') expr      		# addSub
    |   val=expr 'in' '{' member+=expr (',' member+=expr)* '}'	# contains
    |   val=expr 'between' min=expr 'and' max=expr    # range
    |   expr op=('<'|'<='|'>'|'>='|'lt'|'le'|'gt'|'ge') expr     # relational
    |   expr op=('=='|'!='|'eq'|'ne') expr  # equality
    |   expr op=('and'|'&&') expr   		# logicalAnd
    |   expr op=('or'|'||') expr    		# logicalOr
    |   '(' expr ')'            # parens
    |   '#' DATETIME '#'		# timestamp
    |   '#' TIME '#'			# timeonly
    |   '#' DATE '#'			# dateonly
    |   '#' PERIOD '#'			# duration
    |   UINT                    # integer
    |   DECIMAL                 # decimal
    |   CHAR                    # character
    |   STRING                  # string
    |   'exists' var			# exist
    |   var                     # variable // lowest priority so variables do not shadow keywords
    ;

index: '[' UINT ']' ;

pred: '[' ID '==' expr ']' ;

qual: ID (index | pred)? ;

/** 
 * $=variable, %=code symbolic name, in=incoming message, out=outgoing message, this=local scope
 */
var: scope=('$'|'^'|'in.'|'out.'|'this.')? qual ('.' qual )*;

DATETIME: DATE 'T' TIME ;

DATE: DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT ;

TIME: DIGIT DIGIT ':' DIGIT DIGIT (':' DIGIT DIGIT)? ('.' DIGIT+)? TZD ;

fragment
TZD: ('Z' | SIGN DIGIT DIGIT? ':' DIGIT DIGIT );

PERIOD: 'P' (DIGIT+ 'Y')? (DIGIT+ 'M')? (DIGIT+ 'W')? (DIGIT+ 'D')? ('T' (DIGIT+ 'H')? (DIGIT+ 'M' )? (DIGIT+ 'S')?)? ;

DECIMAL: DIGIT+ '.' DIGIT+ ;

UINT: DIGIT+;

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



