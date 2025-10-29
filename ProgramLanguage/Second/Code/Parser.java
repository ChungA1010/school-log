// Parser.java
// Parser for language S

public class Parser {
    Token token;          // current token 
    Lexer lexer;
    String funId = "";

    public Parser(Lexer scan) { 
        lexer = scan;		  
        token = lexer.getToken(); // get the first token
    }
  
    private String match(Token t) {
        String value = token.value();
        if (token == t)
            token = lexer.getToken();
        else
            error(t);
        return value;
    }

    private void error(Token tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token=lexer.getToken();
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token=lexer.getToken();
    }
  
    public Command command() {
    // <command> ->  <decl> | <function> | <stmt>
	    if (isType()) {
	        Decl d = decl();
	        return d;
	    }
	/*
	    if (token == Token.FUN) {
	        Function f = function();
	        return f;
	    }
	*/
	    if (token != Token.EOF) {
	        Stmt s = stmt();
            return s;
	    }
	    return null;
    }

    private Decl decl() {
    // <decl>  -> <type> id [=<expr>]; 
        Type t = type();
	    String id = match(Token.ID);
	    Decl d = null;
	    if (token == Token.ASSIGN) {
	        match(Token.ASSIGN);
            Expr e = expr();
	        d = new Decl(id, t, e);
	    } else 
            d = new Decl(id, t);

	    match(Token.SEMICOLON);
	    return d;
    }

    private Decls decls () {
    // <decls> -> {<decl>}
        Decls ds = new Decls ();
	    while (isType()) {
	        Decl d = decl();
	        ds.add(d);
	    }
        return ds;             
    }

/*
    private Function function() {
    // <function>  -> fun <type> id(<params>) <stmt> 
        match(Token.FUN);
	    Type t = type();
	    String str = match(Token.ID);
	    funId = str; 
	    Function f = new Function(str, t);
	    match(Token.LPAREN);
        if (token != Token.RPAREN)
            f.params = params();
	    match(Token.RPAREN);
	    Stmt s = stmt();		
	    f.stmt = s;
	    return f;
    }

    private Decls params() {
	    Decls params = new Decls();
        
		// parse declrations of parameters

        return params;
    }

*/

    private Type type () {
    // <type>  ->  int | bool | void | string 
        Type t = null;
        switch (token) {
	    case INT:
            t = Type.INT; break;
        case BOOL:
            t = Type.BOOL; break;
        case VOID:
            t = Type.VOID; break;
        case STRING:
            t = Type.STRING; break;
        default:
	        error("int | bool | void | string");
	    }
        match(token);
        return t;       
    }
  
    private Stmt stmt() {
    // <stmt> -> <block> | <assignment> | <ifStmt> | <whileStmt> | ...
        Stmt s = new Empty();
        switch (token) {
	    case SEMICOLON:
            match(token.SEMICOLON); return s;
        case LBRACE:			
	        match(Token.LBRACE);		
            s = stmts();
            match(Token.RBRACE);	
	        return s;
        case IF: 	// if statement 
            s = ifStmt(); return s;
        case WHILE:      // while statement 
            s = whileStmt(); return s;
        case ID:	// assignment
            s = assignment(); return s;
	    case LET:	// let statement 
            s = letStmt(); return s;
	    case READ:	// read statement 
            s = readStmt(); return s;
	    case PRINT:	// print statment 
            s = printStmt(); return s;
	    case RETURN: // return statement 
            s = returnStmt(); return s;
        default:  
	        error("Illegal stmt"); return null; 
	    }
    }
  
    private Stmts stmts () {
    // <block> -> {<stmt>}
        Stmts ss = new Stmts();
	    while((token != Token.RBRACE) && (token != Token.END))
	        ss.stmts.add(stmt()); 
        return ss;
    }

    private Let letStmt () {
    // <letStmt> -> let <decls> in <block> end
	    match(Token.LET);	
        Decls ds = decls();
	    match(Token.IN);
        Stmts ss = stmts();
        match(Token.END);	
        match(Token.SEMICOLON);
        return new Let(ds, null, ss);
    }

    // TODO: [Complete the code of readStmt()]
    private Read readStmt() {
    // <readStmt> -> read id;
    //
    // parse read statement
    //
        match(Token.READ);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.SEMICOLON);
	return new Read(id);
    }
    //read 문장을 해석하는 함수인 readStmt()이다.
    //read문은 read키워드 다음에 식별자 하나가 오고 세미콜론으로 끝난다.
    //현재 토큰이 read인지를 match()함수를 통해 검사하고 다음 토큰으로 이동 시킨다.
    //그런 다음 match(Token.ID)로 다음 토큰이 식별자인지 확인하고 그 문자열값을 반환한다.
    //반환된 식별자 문자열로 new Identifier객체를 만들어서 AST의 노드로 저장한다.
    //마지막으로 세미콜론으로 끝나는지 match()함수를 통해 확인하고 다음 토큰으로 넘긴다.
    //완성된 read노드를 반환하며 끝낸다.

    // TODO: [Complete the code of printStmt()]
    private Print printStmt() {
    // <printStmt> -> print <expr>;
    //
    // parse print statement
    // 
        match(Token.PRINT);
        Expr e = expr();
        match(Token.SEMICOLON);
	return new Print(e);
    }
    //print문장을 파싱해서 ast트리를 만드는 부분
    //print문장은 print키워드 + 표현식인 expr + 세미콜론
    // 현재 읽고 있는 토큰이 print인지 match함수를 통해 확인하고 다음 토큰으로 넘김.
    //표현식을 expr()함수를 통해 파싱, 출력할 대상을 ast형태로 만들어서 e에 저장.
    //마지막으로 세미콜론이 오는지 match함수를 통해서 확인하고 다음 토큰으로 넘김
    //완성된 print노드를 반환하며 끝낸다.

    private Return returnStmt() {
    // <returnStmt> -> return <expr>; 
        match(Token.RETURN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Return(funId, e);
    }

    private Stmt assignment() {
    // <assignment> -> id = <expr>;   
        Identifier id = new Identifier(match(Token.ID));
	/*
	    if (token == Token.LPAREN)    // function call 
	        return call(id);
	*/

        match(Token.ASSIGN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Assignment(id, e);
    }

/*
    private Call call(Identifier id) {
    // <call> -> id(<expr>{,<expr>});
    //
    // parse function call
    //
	return null;
    }
*/

    private If ifStmt () {
    // <ifStmt> -> if (<expr>) then <stmt> [else <stmt>]
        match(Token.IF);
	    match(Token.LPAREN);
        Expr e = expr();
	    match(Token.RPAREN);
        match(Token.THEN);
        Stmt s1 = stmt();
        Stmt s2 = new Empty();
        if (token == Token.ELSE){
            match(Token.ELSE); 
            s2 = stmt();
        }
        return new If(e, s1, s2);
    }

    // TODO: [Complete the code of whileStmt()]
    private While whileStmt () {
    // <whileStmt> -> while (<expr>) <stmt>
    //
    // parse while statement
    //
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
        Stmt s = stmt();
        return new While(e, s);
    }
    //while반복문을 파싱하는 함수
    //while + 왼쪽 괄호 + 조건식expr + 오른쪽 괄호 + 반복 문장 stmt
    //while키워드가 왔는지 match()함수를 통해 확인하고 다음 토큰으로 넘김
    //왼쪽 괄호가 왔는지 match()함수를 통해 확인하고 다음 토큰으로 넘김
    //expr()함수를 호출해서 조건식expr을 파싱, 조건 부분의 트리를 만들어 e에 저장
    //오른쪽 괄호가 왔는지 match()함수를 통해 확인하고 다음 토큰으로 넘김
    //stmt()함수를 호출해서 반복 분장stmt를 파싱, 반복 부분의 트리를 만들어 s에 저장
    // 모든 구성요소 e,s를 합쳐서 하나의 while AST노드로 만들어 반환
    
    private Expr expr () {
    // <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false
        switch (token) {
	    case NOT:
	        Operator op = new Operator(match(token));
	        Expr e = expr();
            return new Unary(op, e);
        case TRUE:
            match(Token.TRUE);
            return new Value(true);
        case FALSE:
            match(Token.FALSE);
            return new Value(false);
        }

        Expr e = bexp();
        // TODO: [Complete the code of logical operations for <expr> -> <bexp> {& <bexp> | '|'<bexp>}]
		//
		// parse logical operations
		//
        if(token == Token.AND || token == Token.OR){
            Operator op = new Operator(match(token));
            Expr right = bexp();
            e = new Binary(op, e, right);
        }
        //and, or 논리식을 AST구조로 만드는 역할
        //현재 읽고 있는 토큰이 논리 연산자인지 token == Token.AND, token == Token.OR로 확인
        //만약 and인 경우 match()함수로 현재 토큰을 소비하고 그 연산자를 AST용 Operator객체로 저장
        //그리고 오른쪽 피연사자를 bexp()함수를 통해서 파싱.
        //이렇게 읽은 왼쪽 식e, 논리 연산자 op, 오른쪽 식 right를 하나로 합쳐서 새로운 이항 연산자 노드로 만든다.
        //or도 같은 방식으로 새로운 Binary노드로 만들어 e에 넣는다. 
        return e;
    }

    // TODO: [Complete the code of bexp()]
    private Expr bexp() {
        // <bexp> -> <aexp> [ (< | <= | > | >= | == | !=) <aexp> ]
        Expr e = aexp();
	//
	// parse relational operations
	//
        if(token == Token.LT || token == Token.LTEQ || token == Token.GT || token == Token.GTEQ || token == Token.EQUAL || token == Token.NOTEQ){
            Operator op = new Operator(match(token));
            Expr right = aexp();
            e = new Binary(op, e, right);
        }
        return e;
    }
    //bexp()함수를 파싱해 AST구조로 만드는 함수
    //먼저 시작하는 산술식 aexp를 e로 받는다.
    //다음에 비교 연산자가 들어오는지 확인한다. 비교연산자가 안들어오면 산술식 그대로 반환.
    //비교 연산자 받았으면 연산자를 소비하고 AST용 Operator객체로 저장
    //오른쪽 산술식 aexp를 읽는다.
    //지금까지 읽은 왼쪽식 e와 연산자 op, 오른쪽 식 right을 묶어서 이항 연산 트리 노드를 만든다.

    private Expr aexp () {
        // <aexp> -> <term> { + <term> | - <term> }
        Expr e = term();
        while (token == Token.PLUS || token == Token.MINUS) {
            Operator op = new Operator(match(token));
            Expr t = term();
            e = new Binary(op, e, t);
        }
        return e;
    }
  
    private Expr term () {
        // <term> -> <factor> { * <factor> | / <factor>}
        Expr t = factor();
        while (token == Token.MULTIPLY || token == Token.DIVIDE) {
            Operator op = new Operator(match(token));
            Expr f = factor();
            t = new Binary(op, t, f);
        }
        return t;
    }
  
    private Expr factor() {
        // <factor> -> [-](id | <call> | literal | '('<aexp> ')')
        Operator op = null;
        if (token == Token.MINUS) 
            op = new Operator(match(Token.MINUS));

        Expr e = null;
        switch(token) {
        case ID:
            Identifier v = new Identifier(match(Token.ID));
            e = v;
            if (token == Token.LPAREN) {  // function call
                match(Token.LPAREN); 
                Call c = new Call(v,arguments());
                match(Token.RPAREN);
                e = c;
            } 
            break;
        case NUMBER: case STRLITERAL: 
            e = literal();
            break; 
        case LPAREN: 
            match(Token.LPAREN); 
            e = aexp();       
            match(Token.RPAREN);
            break; 
        default: 
            error("Identifier | Literal"); 
        }

        if (op != null)
            return new Unary(op, e);
        else return e;
    }
  
    private Exprs arguments() {
    // arguments -> [ <expr> {, <expr> } ]
        Exprs es = new Exprs();
        while (token != Token.RPAREN) {
            es.add(expr());
            if (token == Token.COMMA)
                match(Token.COMMA);
            else if (token != Token.RPAREN)
                error("Exprs");
        }  
        return es;  
    }

    private Value literal( ) {
        String s = null;
        switch (token) {
        case NUMBER:
            s = match(Token.NUMBER);
            return new Value(Integer.parseInt(s));
        case STRLITERAL:
            s = match(Token.STRLITERAL);
            return new Value(s);
        }
        throw new IllegalArgumentException( "no literal");
    }
 
    private boolean isType( ) {
        switch(token) {
        case INT: case BOOL: case STRING: 
            return true;
        default: 
            return false;
        }
    }
    
    public static void main(String args[]) {
	    Parser parser;
        Command command = null;
	    if (args.length == 0) {
	        System.out.print(">> ");
	        Lexer.interactive = true;
	        parser  = new Parser(new Lexer());
	        do {
	            if (parser.token == Token.EOF) 
		        parser.token = parser.lexer.getToken();

                try {
                    command = parser.command();
		            if (command != null) command.display(0);    // display AST, TODO: [Uncomment this line]
                } catch (Exception e) {
                    System.err.println(e);
                }
		        System.out.print("\n>> ");
	        } while(true);
	    }
    	else {
	        System.out.println("Begin parsing... " + args[0]);
	        parser  = new Parser(new Lexer(args[0]));
	        do {
	            if (parser.token == Token.EOF) 
                    break;

                try {
		             command = parser.command();
		             if (command != null) command.display(0);      // display AST, TODO: [Uncomment this line]
                } catch (Exception e) {
                    System.err.println(e); 
                }
	        } while (command != null);
	    }
    } //main
} // Parser