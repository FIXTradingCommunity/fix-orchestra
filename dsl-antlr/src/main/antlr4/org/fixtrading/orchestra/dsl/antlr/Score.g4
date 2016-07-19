grammar Score;

VAR : '$' ;
THIS : 'this' ;
OUT : 'out' ;
DOT : '.' ;
EQUAL : '==' ;
NOT_EQUAL : '!=' ;
LT : '<' ;
LE : '<=' ;
GE : '>=' ;
GT : '>' ;
ADD : '+' ;
SUB : '-' ;
MUL : '*' ;
DIV : '/' ;
MOD : '%' ;
LPAREN : '(' ;
RPAREN : ')' ;
NOT : '!' ;
OR : ('or' | '||') ;
AND : ('and' | '&&') ;
MUL_ASSIGN : '*=' ;
DIV_ASSIGN : '/=' ;
MOD_ASSIGN : '%=' ;
ADD_ASSIGN : '+=' ;
SUB_ASSIGN : '-=' ;
IN : 'in' ;
LBRACE : '{' ;
RBRACE : '}' ;

anyExpression
    : assignment
    | conditionalOrExpression
    | conditionalAndExpression
	| relationalExpression
    ;

assignment
	:	variable ('=' |'*=' | '/=' | '%=' | '+=' | '-=') simpleExpression
	;

conditionalOrExpression
    : relationalExpression ('or' | '||') relationalExpression
    ;

conditionalAndExpression
    : relationalExpression ('and' | '&&') relationalExpression
    ;

relationalExpression
   : simpleExpression (('==' | '!=' | '<' | '<=' | '>=' | '>') simpleExpression)*
   | simpleExpression 'in' setExpression
   ;

simpleExpression
   : term (('+' | '-') term)*
   ;

term
   : factor (('*' | '/' | '%') factor)*
   ;

setExpression
     : '{' factor (',' factor)* '}'
     ;

factor
   : value
   | variable
   | '(' simpleExpression ')'
   | IntegerLiteral
   | DecimalLiteral
   | StringLiteral
   | CharacterLiteral
   | '!' factor
   ;

value // immutable
    : THIS '.' qualifiedId
    ;

variable // mutable
    : '$' qualifiedId
    | 'out' '.' qualifiedId
    ;


qualifiedId
    : Identifier ('.' Identifier)*
    ;

Identifier
	:	Letter LetterOrDigit*
	;

DecimalLiteral
	:	DecimalNumeral '.' Digits?
	;

IntegerLiteral
	:	DecimalNumeral IntegerTypeSuffix?
	;

fragment
IntegerTypeSuffix
	:	[lL]
	;

fragment
DecimalNumeral
	:	'0'
	|	NonZeroDigit (Digits? | Underscores Digits)
	;

fragment
Digits
	:	Digit (DigitsAndUnderscores? Digit)?
	;

fragment
Digit
	:	'0'
	|	NonZeroDigit
	;

fragment
NonZeroDigit
	:	[1-9]
	;

fragment
DigitsAndUnderscores
	:	DigitOrUnderscore+
	;

fragment
DigitOrUnderscore
	:	Digit
	|	'_'
	;

fragment
Underscores
	:	'_'+
	;


fragment
SignedInteger
	:	Sign? Digits
	;

fragment
Sign
	:	[+-]
	;

// §3.10.3 Boolean Literals

BooleanLiteral
	:	'true'
	|	'false'
	;

// §3.10.4 Character Literals

CharacterLiteral
	:	'\'' SingleCharacter '\''
	|	'\'' EscapeSequence '\''
	;

fragment
SingleCharacter
	:	~['\\]
	;

// §3.10.5 String Literals
StringLiteral
	:	'"' StringCharacters? '"'
	;

fragment
StringCharacters
	:	StringCharacter+
	;
fragment
StringCharacter
	:	~["\\]
	|	EscapeSequence
	;

// §3.10.6 Escape Sequences for Character and String Literals
fragment
EscapeSequence
	:	'\\' [btnfr"'\\]
	;

fragment
Letter
	:	[a-zA-Z_]
	;

fragment
LetterOrDigit
	:	[a-zA-Z0-9_] // Unlike Java, does not allow $, but does allow underscore
	;
//
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip
    ;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;
