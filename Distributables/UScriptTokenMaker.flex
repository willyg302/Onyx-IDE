import java.io.*;
import javax.swing.text.Segment;

/**
 * CHANGE THE FOLLOWING:
 *  - add first line "package unrealeditor;"
 *  - add import "import org.fife.ui.rsyntaxtextarea.*;"
 *  - add "extends AbstractJFlexCTokenMaker" after class declaration
 *  - delete the second zzRefill() function
 *  - delete the second yyReset() function
 *  - delete "zzPushbackPos = " (it should be an error, so find it that way)
 *  - replace "Yytoken" with "org.fife.ui.rsyntaxtextarea.Token" in the yylex() function declaration
 */

%%
%public
%class UScriptTokenMaker
%unicode
%{
	public UScriptTokenMaker() {}

	private void addHyperlinkToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, true);
	}

	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}

	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, false);
	}

	public void addToken(char[] array, int start, int end, int tokenType,
						int startOffset, boolean hyperlink) {
		super.addToken(array, start,end, tokenType, startOffset, hyperlink);
		zzStartRead = zzMarkedPos;
	}

	public String[] getLineCommentStartAndEnd() {
		return new String[] { "//", null };
	}

	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

		resetTokenList();
		this.offsetShift = -text.offset + startOffset;

		// Start off in the proper state.
		int state = Token.NULL;
		switch (initialTokenType) {
			case Token.COMMENT_MULTILINE:
				state = MLC;
				start = text.offset;
				break;
			case Token.COMMENT_DOCUMENTATION:
				state = DOCCOMMENT;
				start = text.offset;
				break;
			default:
				state = Token.NULL;
		}

		s = text;
		try {
			yyreset(zzReader);
			yybegin(state);
			return yylex();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return new DefaultToken();
		}
	}

	private boolean zzRefill() throws java.io.IOException {
		return zzCurrentPos>=s.offset+s.count;
	}

	public final void yyreset(java.io.Reader reader) throws java.io.IOException {
		zzBuffer = s.array;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}


%}

Letter							= ([A-Za-z])
LetterOrUnderscore					= ({Letter}|"_")
Underscores						= ([_]+)
NonzeroDigit						= ([1-9])
BinaryDigit						= ([0-1])
Digit							= ("0"|{NonzeroDigit})
HexDigit						= ({Digit}|[A-Fa-f])
OctalDigit						= ([0-7])
AnyCharacterButApostropheOrBackSlash			= ([^\\'])
AnyCharacterButDoubleQuoteOrBackSlash			= ([^\\\"\n])
EscapedSourceCharacter					= ("u"{HexDigit}{HexDigit}{HexDigit}{HexDigit})
Escape							= ("\\"(([btnfr\"'\\])|([0123]{OctalDigit}?{OctalDigit}?)|({OctalDigit}{OctalDigit}?)|{EscapedSourceCharacter}))
NonSeparator						= ([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\']|"#"|"\\")
IdentifierStart						= ({LetterOrUnderscore}|"$")
IdentifierPart						= ({IdentifierStart}|{Digit}|("\\"{EscapedSourceCharacter}))

LineTerminator						= (\n)
WhiteSpace						= ([ \t\f])

CharLiteral						= ([\']({AnyCharacterButApostropheOrBackSlash}|{Escape})[\'])
UnclosedCharLiteral					= ([\'][^\'\n]*)
ErrorCharLiteral					= ({UnclosedCharLiteral}[\'])
StringLiteral						= ([\"]({AnyCharacterButDoubleQuoteOrBackSlash}|{Escape})*[\"])
UnclosedStringLiteral					= ([\"]([\\].|[^\\\"])*[^\"]?)
ErrorStringLiteral					= ({UnclosedStringLiteral}[\"])

MLCBegin						= "/*"
MLCEnd							= "*/"
DocCommentBegin						= "/**"
LineCommentBegin					= "//"

DigitOrUnderscore					= ({Digit}|[_])
DigitsAndUnderscoresEnd					= ({DigitOrUnderscore}*{Digit})
IntegerHelper						= (({NonzeroDigit}{DigitsAndUnderscoresEnd}?)|"0")
IntegerLiteral						= ({IntegerHelper}[lL]?)

BinaryDigitOrUnderscore					= ({BinaryDigit}|[_])
BinaryDigitsAndUnderscores				= ({BinaryDigit}({BinaryDigitOrUnderscore}*{BinaryDigit})?)
BinaryLiteral						= ("0"[bB]{BinaryDigitsAndUnderscores})

HexDigitOrUnderscore					= ({HexDigit}|[_])
HexDigitsAndUnderscores					= ({HexDigit}({HexDigitOrUnderscore}*{HexDigit})?)
OctalDigitOrUnderscore					= ({OctalDigit}|[_])
OctalDigitsAndUnderscoresEnd				= ({OctalDigitOrUnderscore}*{OctalDigit})
HexHelper						= ("0"(([xX]{HexDigitsAndUnderscores})|({OctalDigitsAndUnderscoresEnd})))
HexLiteral						= ({HexHelper}[lL]?)

FloatHelper1						= ([fFdD]?)
FloatHelper2						= ([eE][+-]?{Digit}+{FloatHelper1})
FloatLiteral1						= ({Digit}+"."({FloatHelper1}|{FloatHelper2}|{Digit}+({FloatHelper1}|{FloatHelper2})))
FloatLiteral2						= ("."{Digit}+({FloatHelper1}|{FloatHelper2}))
FloatLiteral3						= ({Digit}+{FloatHelper2})
FloatLiteral						= ({FloatLiteral1}|{FloatLiteral2}|{FloatLiteral3}|({Digit}+[fFdD]))

ErrorNumberFormat					= (({IntegerLiteral}|{HexLiteral}|{FloatLiteral}){NonSeparator}+)
BooleanLiteral						= ("true"|"false"|"True"|"False")

Separator						= ([\(\)\{\}\[\]])
Separator2						= ([\;,.])

NonAssignmentOperator					= ("+"|"-"|"*"|"/"|"**"|"%"|"$"|"@"|"=="|"~="|"!="|"<"|"<="|">"|">="|"&&"|"||"|"^^"|"~"|"&"|"|"|"^"|">>"|"<<"|"Cross"|"Dot"|"?"|":"|"ClockwiseFrom"|"++"|"--"|">>>")
AssignmentOperator					= ("+="|"-="|"*="|"/="|"$="|"@=")
Operator						= ({NonAssignmentOperator}|{AssignmentOperator})

CurrentBlockTag						= ("author"|"deprecated"|"exception"|"param"|"return"|"see"|"serial"|"serialData"|"serialField"|"since"|"throws"|"version")
ProposedBlockTag					= ("category"|"example"|"tutorial"|"index"|"exclude"|"todo"|"internal"|"obsolete"|"threadsafety")
BlockTag						= ({CurrentBlockTag}|{ProposedBlockTag})
InlineTag						= ("code"|"docRoot"|"inheritDoc"|"link"|"linkplain"|"literal"|"value")

Identifier						= ({IdentifierStart}{IdentifierPart}*)
ErrorIdentifier						= ({NonSeparator}+)

Annotation						= ("@"{Identifier}?)

URLGenDelim						= ([:\/\?#\[\]@])
URLSubDelim						= ([\!\$&'\(\)\*\+,;=])
URLUnreserved						= ({LetterOrUnderscore}|{Digit}|[\-\.\~])
URLCharacter						= ({URLGenDelim}|{URLSubDelim}|{URLUnreserved}|[%])
URLCharacters						= ({URLCharacter}*)
URLEndCharacter						= ([\/\$]|{Letter}|{Digit})
URL							= (((https?|f(tp|ile))"://"|"www.")({URLCharacters}{URLEndCharacter})?)


%state MLC
%state DOCCOMMENT
%state EOL_COMMENT
%%

<YYINITIAL> {

	/* Keywords */

	"abstract"		|
	"allowabstract"		|
	"always"		|
	"assert"		|
	"auto"			|
	"autoexpandcategories"	|
	"automated"		|
	"break"			|
	"case"			|
	"class"			|
	"classgroup"		|
	"client"		|
	"coerce"		|
	"collapsecategories"	|
	"config"		|
	"continue"		|
	"databinding"		|
	"default"		|
	"defaultproperties"	|
	"delegate"		|
	"dependson"		|
	"deprecated"		|
	"do"			|
	"dontcollapsecategories"|
	"duplicatetransient"	|
	"editconst"		|
	"editconstarray"	|
	"editfixedsize"		|
	"editinline"		|
	"editinlinenew"		|
	"editinlinenotify"	|
	"editinlineuse"		|
	"editoronly"		|
	"else"			|
	"event"			|
	"exec"			|
	"export"		|
	"extends"		|
	"final"			|
	"for"			|
	"forcescriptorder"	|
	"foreach"		|
	"function"		|
	"global"		|
	"globalconfig"		|
	"goto"			|
	"hidecategories"	|
	"hidedropdown"		|
	"ignores"		|
	"if"			|
	"implements"		|
	"import"		|
	"inherits"		|
	"init"			|
	"input"			|
	"insert"		|
	"instanced"		|
	"interface"		|
	"interp"		|
	"intrinsic"		|
	"iterator"		|
	"latent"		|
	"local"			|
	"localized"		|
	"native"		|
	"nativereplication"	|
	"new"			|
	"noclear"		|
	"noexport"		|
	"noimport"		|
	"none"			|
	"nontransactional"	|
	"nontransient"		|
	"noteditinlinenew"	|
	"notforconsole"		|
	"notplaceable"		|
	"operator"		|
	"optional"		|
	"out"			|
	"perobjectconfig"	|
	"perobjectlocalized"	|
	"placeable"		|
	"pointer"		|
	"postoperator"		|
	"preoperator"		|
	"private"		|
	"protected"		|
	"reliable"		|
	"repnotify"		|
	"repretry"		|
	"self"			|
	"server"		|
	"showcategories"	|
	"simulated"		|
	"singular"		|
	"skip"			|
	"state"			|
	"static"		|
	"stop"			|
	"super"			|
	"switch"		|
	"transient"		|
	"travel"		|
	"unreliable"		|
	"until"			|
	"var"			|
	"while"			|
	"within"		{ addToken(Token.RESERVED_WORD); }
	"return"		{ addToken(Token.RESERVED_WORD_2); }


	/* Data types. */

	"array"		|
	"bool"		|
	"byte"		|
	"Color"		|
	"const"		|
	"Coords"	|
	"delegate"	|
	"enum"		|
	"float"		|
	"Guid"		|
	"int"		|
	"Matrix"	|
	"name"		|
	"Plane"		|
	"Range"		|
	"Region"	|
	"Rotator"	|
	"Scale"		|
	"string"	|
	"struct"	|
	"Vector"	{ addToken(Token.DATA_TYPE); }


	/* Booleans. */

	{BooleanLiteral}			{ addToken(Token.LITERAL_BOOLEAN); }


	/* Select Functions */

	"`assert"		|
	"`define"		|
	"`else"			|
	"`endif"		|
	"`if"			|
	"`include"		|
	"`isdefined"		|
	"`log"			|
	"`logd"			|
	"`notdefined"		|
	"`undefine"		|
	"`warn"			|
	"Abs"			|
	"ACos"			|
	"AllActors"		|
	"AllClientConnections"	|
	"AllControllers"	|
	"AllNavigationPoints"	|
	"AllOwnedComponents"	|
	"AllPawns"		|
	"ArrayCount"		|
	"Asc"			|
	"ASin"			|
	"Atan"			|
	"BasedActors"		|
	"Caps"			|
	"Ceil"			|
	"ChildActors"		|
	"Chr"			|
	"Clamp"			|
	"CollidingActors"	|
	"ComponentList"		|
	"Cos"			|
	"DumpStateStack"	|
	"DynamicActors"		|
	"EnumCount"		|
	"Exp"			|
	"FClamp"		|
	"FMax"			|
	"FMin"			|
	"FRand"			|
	"GetFuncName"		|
	"InStr"			|
	"Invert"		|
	"JoinArray"		|
	"Left"			|
	"Len"			|
	"Lerp"			|
	"LocalPlayerControllers"|
	"Locs"			|
	"Loge"			|
	"Max"			|
	"Mid"			|
	"Min"			|
	"MirrorVectorByNormal"	|
	"Normal"		|
	"OverlappingActors"	|
	"ParseStringIntoArray"	|
	"RadiusNavigationPoints"|
	"Rand"			|
	"Repl"			|
	"Right"			|
	"Rng"			|
	"Rot"			|
	"Round"			|
	"ScriptTrace"		|
	"Sin"			|
	"Smerp"			|
	"Split"			|
	"SplitString"		|
	"Sqrt"			|
	"Square"		|
	"Tan"			|
	"TouchingActors"	|
	"TraceActors"		|
	"Vect"			|
	"VisibleActors"		|
	"VisibleCollidingActors"|
	"VRand"			|
	"VSize"			{ addToken(Token.FUNCTION); }


	{LineTerminator}				{ addNullToken(); return firstToken; }

	{Identifier}					{ addToken(Token.IDENTIFIER); }

	{WhiteSpace}+					{ addToken(Token.WHITESPACE); }

	/* String/Character literals. */
	{CharLiteral}					{ addToken(Token.LITERAL_CHAR); }
	{UnclosedCharLiteral}				{ addToken(Token.ERROR_CHAR); addNullToken(); return firstToken; }
	{ErrorCharLiteral}				{ addToken(Token.ERROR_CHAR); }
	{StringLiteral}					{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }
	{UnclosedStringLiteral}				{ addToken(Token.ERROR_STRING_DOUBLE); addNullToken(); return firstToken; }
	{ErrorStringLiteral}				{ addToken(Token.ERROR_STRING_DOUBLE); }

	/* Comment literals. */
	"/**/"						{ addToken(Token.COMMENT_MULTILINE); }
	{MLCBegin}					{ start = zzMarkedPos-2; yybegin(MLC); }
	{DocCommentBegin}				{ start = zzMarkedPos-3; yybegin(DOCCOMMENT); }
	{LineCommentBegin}				{ start = zzMarkedPos-2; yybegin(EOL_COMMENT); }

	/* Annotations. */
	{Annotation}					{ addToken(Token.ANNOTATION); }

	/* Separators. */
	{Separator}					{ addToken(Token.SEPARATOR); }
	{Separator2}					{ addToken(Token.IDENTIFIER); }

	/* Operators. */
	{Operator}					{ addToken(Token.OPERATOR); }

	/* Numbers */
	{IntegerLiteral}				{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
	{BinaryLiteral}					{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
	{HexLiteral}					{ addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
	{FloatLiteral}					{ addToken(Token.LITERAL_NUMBER_FLOAT); }
	{ErrorNumberFormat}				{ addToken(Token.ERROR_NUMBER_FORMAT); }

	{ErrorIdentifier}				{ addToken(Token.ERROR_IDENTIFIER); }

	/* Ended with a line not in a string or comment. */
	<<EOF>>						{ addNullToken(); return firstToken; }

	/* Catch any other (unhandled) characters and flag them as identifiers. */
	.						{ addToken(Token.ERROR_IDENTIFIER); }

}


<MLC> {
	[^hwf\n\*]+	{}
	{URL}		{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_MULTILINE); start = zzMarkedPos; }
	[hwf]		{}
	\n		{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }
	{MLCEnd}	{ yybegin(YYINITIAL); addToken(start,zzStartRead+1, Token.COMMENT_MULTILINE); }
	\*		{}
	<<EOF>>		{ addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE); return firstToken; }
}

<DOCCOMMENT> {
	[^hwf\@\{\n\<\*]+		{}
	{URL}				{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_DOCUMENTATION); start = zzMarkedPos; }
	[hwf]				{}
	"@"{BlockTag}			{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addToken(temp,zzMarkedPos-1, Token.COMMENT_KEYWORD); start = zzMarkedPos; }
	"@"				{}
	"{@"{InlineTag}[^\}]*"}"	{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addToken(temp,zzMarkedPos-1, Token.COMMENT_KEYWORD); start = zzMarkedPos; }
	"{"				{}
	\n				{ addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); return firstToken; }
	"<"[/]?({Letter}[^\>]*)?">"	{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_DOCUMENTATION); addToken(temp,zzMarkedPos-1, Token.COMMENT_MARKUP); start = zzMarkedPos; }
	\<				{}
	{MLCEnd}			{ yybegin(YYINITIAL); addToken(start,zzStartRead+1, Token.COMMENT_DOCUMENTATION); }
	\*				{}
	<<EOF>>				{ yybegin(YYINITIAL); addToken(start,zzEndRead, Token.COMMENT_DOCUMENTATION); return firstToken; }
}

<EOL_COMMENT> {
	[^hwf\n]+	{}
	{URL}		{ int temp=zzStartRead; addToken(start,zzStartRead-1, Token.COMMENT_EOL); addHyperlinkToken(temp,zzMarkedPos-1, Token.COMMENT_EOL); start = zzMarkedPos; }
	[hwf]		{}
	\n		{ addToken(start,zzStartRead-1, Token.COMMENT_EOL); addNullToken(); return firstToken; }
	<<EOF>>		{ addToken(start,zzStartRead-1, Token.COMMENT_EOL); addNullToken(); return firstToken; }
}