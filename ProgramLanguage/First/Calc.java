import java.io.*;

class Calc {
    int token; int value; int ch;
    private PushbackInputStream input;
    final int NUMBER=256;

    Calc(PushbackInputStream is) {
        input = is;
    }

    int getToken( )  { /* tokens are characters */
        while(true) {
            try  {
	            ch = input.read();
                if (ch == ' ' || ch == '\t' || ch == '\r') ;
                else 
                    if (Character.isDigit(ch)) {
                        value = number( );
	                    input.unread(ch);
		                return NUMBER;
	          }
	          else return ch;
	  } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private int number( )  {
    /* number -> digit { digit } */
        int result = ch - '0';
        try  {
            ch = input.read();
            while (Character.isDigit(ch)) {
                result = 10 * result + ch -'0';
                ch = input.read(); 
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }

    void error( ) {
        System.out.printf("parse error : %d\n", ch);
        //System.exit(1);
    }

    void match(int c) {
        if (token == c) 
	    token = getToken();
        else error();
    }

    void command( ) {
    /* command -> expr '\n' */
        //int result = aexp();		// TODO: [Remove this line!!]	
        Object result = expr();  // TODO: [Use this line for solution] -> 수리연산 및 논리연산까지 하기 위해서
        if (token == '\n') /* end the parse and print the result */ //토큰이 '줄바꿈'값을 갖고 있다면 분석을 끝내고 결과를 프린트 한다.
	    System.out.println(result); //token이 '\n'을 갖고 있음으로 result를 프린트한다.
        else error();   // 만약 token이 '\n'을 갖고 있지 않다면 error함수를 호출한다.
    }
    
    Object expr() {
    /* <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false */
    	Object result;
    	//result = ""; // TODO: [Remove this line!!]    //내용이 없었음으로 return할 result값을 초기화 했던 것임.
    	if (token == '!'){  //만약 token이 !를 갖고 있다면
    		// !<expr>
    		match('!');     //match함수에 '!'를 인자로 호출한다.
    		result = !(boolean) expr(); 
            // 현재 토큰이 !라면 → match('!')로 소비.
            // 그 다음 expr() 전체를 재귀적으로 다시 호출.
            // 나온 결과를 boolean으로 캐스팅하고 반대로 뒤집음.
            // 즉, 부정 논리 연산자 처리.
    	}
    	else if (token == 't'){ //만약 token이 't'를 갖고 있다면
    		// true
            // 논리 상수 리터럴 처리
    		match('t'); // match함수에 't'를 인자로 호출한다.
    		result = (boolean)true; //result값에 true를 넣는다. 당연히 boolean으로 캐스팅
    	}
    	else if (token == 'f'){ //token이 f값을 갖고 있다면
            // false
            // 논래 상수 리터럴 처리
            match('f');     //match함수에 'f'를 인자로 호출한다.
    		result = (boolean)false;    //result값에 false를 넣는다. 당연히 boolean으로 캐스팅
    	}
    	else {      //token이 !도 t도 f도 아니라면 <bexp>를 갖고 있다는 뜻이다.
    		/* <bexp> {& <bexp> | '|'<bexp>} */
    		result = bexp();    //그러므로 bexp()함수를 호출해 result에 넣어준다.
    		while (token == '&' || token == '|') {
    			if (token == '&'){
    				// TODO: [Fill in your code here]
                    match('&');
                    Object rhs = bexp();
                    result = (boolean)result && (boolean)rhs;
    			}
    			else if (token == '|'){
    				// TODO: [Fill in your code here]
                    match('|');
                    Object rhs = bexp();
                    result = (boolean)result || (boolean)rhs;
    			}
    		}
            // 첫 번째 비교식(bexp())을 읽어들임.
            // 그 다음 &나 |가 계속 이어지면 반복해서 읽음.
            // 왼쪽에서 오른쪽으로 순차 계산.
            // 논리곱 / 논리합 처리.
    	}
    	return result;
	}

    Object bexp( ) {    //비교식
    /* <bexp> -> <aexp> [<relop> <aexp>] */
    	Object result;
    	//result = ""; // TODO: [Remove this line!!] //내용이 없었을 때 return하기 위한 result함수를 초기화 했던 줄
    	int aexp1 = aexp();     //aexp() 산술연산 함수를 통한 값을 int형 aexp1으로 저장한다.
    	if (token == '<' || token == '>' || token == '=' || token == '!'){ // <relop>
            //관계 연산자가 token인 경우 연산자 문자열 처리를 위해 relop()으로 넘긴다.
    		/* Check each string using relop(): "<", "<=", ">", ">=", "==", "!=" */
    		// TODO: [Fill in your code here]
            String op = relop();      // 연산자 문자열, relop()에서 반환한 문자열을 op변수로 저장한다.
            int aexp2 = aexp();       // 연산자 이후에 나오는 값을 aexp2변수로 저장한다.
            switch (op) {   // op에 저장된 값을 case로 나눈다.
                case "<":  result = (aexp1 < aexp2); break; //각 연산자 케이스에 따라 관계 연산을 진행한다.
                case "<=": result = (aexp1 <= aexp2); break;
                case ">":  result = (aexp1 > aexp2); break;
                case ">=": result = (aexp1 >= aexp2); break;
                case "==": result = (aexp1 == aexp2); break;
                case "!=": result = (aexp1 != aexp2); break;
                default:    // 관계연산자 이외의 연산자인 경우 error()를 호출한다.
                    error();
                    result = false;     //그런다음 result는 false를 반환한다.
            }
    	}
		else {  
			result = aexp1; // 뒤에 연산자가 나오지 않았음으로 result에 aexp1만 반환한다.
		}
    	return result;		//result를 반환한다.
	}   

    String relop() {    //관계연산자에 대한 relop함수를 정의한다. 관계연산자를 반환해야 함으로 String타입으로 반환한다.
    /* <relop> -> ( < | <= | > | >= | == | != ) */    	
    	String result = ""; //  result초기값을 지정한다. 관계연산자가 나올 부분이 관계연산자가 아닌것이 나올 경우 빈문자열을 반환해야 하기 때문.
    	// TODO: [Fill in your code here]
        if (token == '<') { //token이 '<' 관계연산자를 갖고 있다면 
            match('<'); //match함수를 통해 다음 token을 가져온다.   
            if (token == '=') { match('='); result = "<="; }    //다음 토큰이 =과 같은지 비교하여 =인 경우 <=로 result를 반환한다.
            else result = "<";  //다음 토큰이 '='이 아닌 경우 result에 '<'를 반환한다.
        }
        else if (token == '>') {    // token이 '<' 관계연산자를 갖고 있다면
            match('>'); // match함수를 통해 다음 token을 가져온 뒤 
            if (token == '=') { match('='); result = ">="; }    //다음 토큰이 =과 같은지 비교하여 =인 겨웅 >=로 result를 반환한다.
            else result = ">";  //다음 토큰이 '='가 아닐경우 '>'를 반환한다.
        }   
        else if (token == '=') {    //token이 '=' 관계연산자를 갖고 있다면
            match('='); //match함수를 통해 다음 token을 가져온 뒤
            if (token == '=') { match('='); result = "=="; }    //다음token이 =인지 확인하여 '='인 경우 ==으로 result를 반환한다.
            else error(); //뒤 토큰에 =이 없다면 잘못된 관계연산자임으로 error()를 호출한다.
        }
        else if (token == '!') {    //만약 토큰이 !를 갖고 있다면
            match('!'); // match함수를 통해 다음 token을 가져온 뒤
            if (token == '=') { match('='); result = "!="; }    //다음 token이 =인지 확인하여 '='인 경우 '!='으로 result를 반환한다.
            else error();   //뒤 토큰에 '='이 없다면 error()를 호출한다.
        }
        else {  //관계연산자는 위의 경우로 모두 나오는데 이외의 연산자가 나온다면 error()를 반환한다.
            error();    
        }
    	return result;
	}
    
    // TODO: [Modify code of aexp() for <aexp> -> <term> { + <term> | - <term> }]
    int aexp() {    // aexp()함수는 +,- 연산을 처리하는 함수이다.
    /* expr -> term { '+' term } */
        int result = term();    // 일단 result값에 곱과 나눗셈 처리가 된 term()값을 넣는다.
        while (token == '+' || token =='-') {   //token이 +인지 -인지 비교를 해서 참이 나온다면 반복을 돌린다.
            if (token == '+'){  // token이 +를 갖고 있다면
                match('+'); //match함수를 통해서 다음 토큰으로 넘기고
                result += term();   // 결과값에 term()함수의 결과값을 더해준다.
                //토큰이 다음 토큰으로 넘어가 있음으로 term()을 통해서 나오는 결과값을 result에 더해준다.
            }else{  //token이 -를 갖고 있다면 
                match('-'); //match함수를 통해서 다음 토큰으로 넘기고
                result -= term();   //결과값에 term()함수의 결과값을 빼준다.
                //토큰이 다음 토큰으로 넘어가 있음으로 term()을 통해서 나오는 결과값을 result에 빼준다.
            }
        }
        return result;  // result를 반환한다.
    }

 // TODO: [Modify code of term() for <term> -> <factor> { * <factor> | / <factor>}]
    int term( ) {   //term()은 곱셈, 나눗셈을 처리하는 함수이다.
    /* term -> factor { '*' factor } */
       int result = factor();   //result는 어떠한 숫자값을 갖는 factor()값을 저장해야 함으로 int형 result에 저장한다.
       while (token == '*' || token == '/') {   //토큰이 *이나 /을 갖고 있다면 반복을 계속한다.
           if(token == '*'){    //만약 token이 *라면
            match('*'); //match함수를 통해서 다음 토큰으로 넘기고
            result *= factor(); //결과값에 factor()함수를 통해 나온 값을 result에 넣는다.
           }else{   // 만약 token이 /라면
            match('/'); //match함수를 통해서 다음 토큰으로 넘기고
            int rhs = factor(); //factor()를 통해 나온 숫자값을 int형 rhs값에 넣어준다.
            if (rhs == 0) error();  //만약 rhs값이 0이라면, 즉 factor()값이 0이 나왔다는 뜻이고 0으로 숫자를 나누는것은 불가능함으로 error()를 반환한다.
            result /= rhs;  //result에 rhs를 나눈 몫을 넣어준다.
           }
       }
       return result;   //term()함수를 통한 결과값인 result를 반환한다.
    }

    int factor() {  //factor()함수는 숫자연산을 통한 결과값 또는 숫자를 반환하는 함수다.
    /* factor -> '(' expr ')' | number */
    // TODO: Modify this code to factor -> [-] ( '(' expr ')' | number )
        int result = 0; //result는 0으로 초기화한다.
        if (token == '-') { // 단항 음수 처리 - 그냥 뺄셈이 아니라 이 뒤에 오는 정수가 음수임을 알려주는 것.
            match('-'); // match함수를 통해 토큰 비교 후 다음 토큰으로 넘긴다.
            result = -factor(); //이후 result값에 factor()를 재귀하여 나온 결과값에 부호를 바꾸어 넣어준다.
        }
        else if (token == '(') {    //token이 (를 갖고 있는 경우    
            match('('); //match함수를 통해서 (를 비교하고 다음 토큰으로 넘긴다.
            result = aexp();   // 괄호 안은 aexp 전체, result값에 aexp()결과값을 넣는다.   
            match(')'); // 이후에 괄호가 닫혀야 함으로 다음 토큰이 )인지 확인후 다시 다음 토큰으로 넘긴다.
        }
        else if (token == NUMBER) { //token이 NUMBER값, 즉 256인지 확인한다.
            result = value; //token이 NUMBER값을 갖고 있다면 저장해놓은 정수값 value를 result에 넣는다.
            match(NUMBER);  //그리고 match함수를 통해서 NUMBER를 비교하고 다음 토큰으로 넘긴다.
        }
        else {  //factor()함수에 온 토큰이 위의 조건을 모두 만족하지 않는다면 오류임 
            error(); // 예외 처리
        }
        return result;
    }

    void parse( ) { //이 코드는 파서를 구현하는 것임. 
        token = getToken(); // 첫 번째 토큰을 가져오고
        command();          // 파싱하는 command()를 호출한다.
    }

    public static void main(String args[]) {    //main함수
        Calc calc = new Calc(new PushbackInputStream(System.in));
        while(true) {   //문장이 끝날때까지 반복.
            System.out.print(">> ");    // 입력값을 받을 부분에 >>를 출력한다.
            calc.parse();   //입력받은 값이 문법에 맞는지 파싱한다.
        }
    }
}