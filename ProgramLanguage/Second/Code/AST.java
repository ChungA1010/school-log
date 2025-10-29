// AST.java
// AST for S
import java.util.*;

class Indent {
    public static void display(int level, String s) {
        String tab = "";
        System.out.println();
        for (int i=0; i<level; i++)
            tab = tab + "  ";
        System.out.print(tab + s);
   }
} 

abstract class Command {
    // Command = Decl | Function | Stmt
    Type type =Type.UNDEF;
    public void display(int l) {  }
}

class Decls extends ArrayList<Decl> {
    // Decls = Decl*

    Decls() { super(); };
    Decls(Decl d) {
	    this.add(d);
    }
    // TODO: [Insert the code of display()]
	public void display(int level){
		// Fill code here
        for (Decl d : this)
            d.display(level + 1);
	}
    //decl 리스트 출력하는 부분
    //this리스트에 들어있는 모든 decl객체를 하나씩 꺼내서
    //각각을 d라는 이름으로 참조하여 반복실행하는 구조이다. 
    //Decl객체의 display()를 호출하여 decls안의 decl노드를 차례로 출력한다.
}

class Decl extends Command {
    // Decl = Type type; Identifier id 
    Identifier id;
    Expr expr = null;
    int arraysize = 0;

    Decl (String s, Type t) {
        id = new Identifier(s); type = t;
    } // declaration 

    Decl (String s, Type t, int n) {
        id = new Identifier(s); type = t; arraysize = n;
    } // array declaration 

    Decl (String s, Type t, Expr e) {
        id = new Identifier(s); type = t; expr = e;
    } // declaration
   
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Decl");
        Indent.display(level + 1, "Type: " + type);
        Indent.display(level + 1, "Identifier: " + id);
        if (expr != null) {
            expr.display(level + 1);
        }
    } 
    //변수 선언 노드의 구조를 계층적으로 출력하는 함수
    // AST안에서 선언문을 트리 형태로 보여주는 역할
    //일단 제일 먼저 현재 level에 Decl을 출력하고
    //다음줄부터 level+1의 위치에 type과 그 정보
    //그 다음줄에 level+1의 위치에 Identifier과 실제 id를 출력하면 된다.   
}

class Functions extends ArrayList<Function> {
    // Functions = Function*
}

class Function extends Command  {
    // Function = Type type; Identifier id; Decls params; Stmt stmt
    Identifier id;
    Decls params;
    Stmt stmt;
   
    Function(String s, Type t) { 
        id = new Identifier(s); type = t; params = null; stmt = null;
    }

    public String toString ( ) { 
       return id.toString()+params.toString(); 
    }
}

class Type {
    // Type = int | bool | string | fun | array | except | void
    final static Type INT = new Type("int");
    final static Type BOOL = new Type("bool");
    final static Type STRING = new Type("string");
    final static Type VOID = new Type("void");
    final static Type FUN = new Type("fun");
    final static Type ARRAY = new Type("array");
    final static Type EXC = new Type("exc");
    final static Type RAISEDEXC = new Type("raisedexc");
    final static Type UNDEF = new Type("undef");
    final static Type ERROR = new Type("error");
    
    protected String id;
    protected Type(String s) { id = s; }
    public String toString ( ) { return id; }
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Type: " + id);
    }
    //자료형을 표현하는 클래스로
    //현재 level에 type와 type id를 출력하는 역할을 한다. 
}

class ProtoType extends Type {
   // defines the type of a function and its parameters
   Type result;  
   Decls params;
   ProtoType (Type t, Decls ds) {
      super(t.id);
      result = t;
      params = ds;
   }
}

abstract class Stmt extends Command {
    // Stmt = Empty | Stmts | Assignment | If  | While | Let | Read | Print
}

class Empty extends Stmt {
    public void display (int level) {
        Indent.display(level, "Empty");
     }
}

class Stmts extends Stmt {
    // Stmts = Stmt*
    public ArrayList<Stmt> stmts = new ArrayList<Stmt>();
    
    Stmts() {
	    super(); 
    }

    Stmts(Stmt s) {
	     stmts.add(s);
    }
    // TODO: [Insert the code of display()]
    public void display(int level) {
        for (Stmt s : stmts)
            s.display(level);
    }
    // 이 코드는 Stmts클래스의 display()매서드
    //여러 개의 문장을 묶어서 출력하는 역할
    //괄호 안에 있는 여러개의 문장이 모여 있는 부분을 나타내는 클래스
    //stmts리스트에 들어 있는 문장들을 하나씩 꺼내서 s라는 변수에 담아 반복 실행
    //display매서드를 통해서 level위치에 출력
}

class Assignment extends Stmt {
    // Assignment = Identifier id; Expr expr
    Identifier id;
    Array ar = null;
    Expr expr;

    Assignment (Identifier t, Expr e) {
        id = t;
        expr = e;
    }

    Assignment (Array a, Expr e) {
        ar = a;
        expr = e;
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Assignment");
        Indent.display(level + 1, "Identifier: " + id);
        expr.display(level + 1);
    }
    //Assignment의 AST출력 부분.
    // 일단 Assignment라는 줄을 출력하고
    // 다음줄에 level+1의 위치에서 Identifier와 id를 출력하여 보여준다.
    //이후에 나오는 부분은 오른쪽의 값, 수식을 출력하는 부분.
    //expr은 value, binary, Unary, Identifier타입을 가질수 있고
    //expr.display()호출하여 하위 클래스의 display()가 실행된다.
}

class If extends Stmt {
    // If = Expr expr; Stmt stmt1, stmt2;
    Expr expr;
    Stmt stmt1, stmt2;
    
    If (Expr t, Stmt tp) {
        expr = t; stmt1 = tp; stmt2 = new Empty( );
    }
    
    If (Expr t, Stmt tp, Stmt ep) {
        expr = t; stmt1 = tp; stmt2 = ep; 
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "If");
        expr.display(level + 1);
        stmt1.display(level + 1);
        if (!(stmt2 instanceof Empty)) {
            stmt2.display(level + 1);
        }
    }
    //if의 ast 노드 출력 함수
    //일단 if문은 if (expr) then stmt1 else stmt2의 구조임
    //그래서 먼저 if문 노드가 시작된다는 것을 출력하여 if라는 문자열을 찍는다.
    //그런다음 조건식 부분인 expr을 level+1의 위치에 출력한다.
    //그리고 다음에 나오는 문장1을 level+1의 위치에 찍어준다.
    //if문에 문장2가 나오지 않을 수도있음으로 stmt2가 empty객체인지 검사해서 비어 있지 않을 때만 출력
    //만약 비어있지 않다면 stmt2를 level+1위치에 출력한다.
}

class While extends Stmt {
    // While = Expr expr; Stmt stmt;
    Expr expr;
    Stmt stmt;

    While (Expr t, Stmt b) {
        expr = t; stmt = b;
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "While");
        expr.display(level + 1);
        Indent.display(level + 1, "Stmts");
        stmt.display(level + 2);
    }
    //while의 구조를 ast 트리 형태로 출력하는 역할
    //먼저 while문임을 while을 출력하여 알리고
    //level + 1 위치에서 조건식인 expr을 출력한다.
    //level+1 위치에서 반복을 실행할 본문인 stmts를 출력하고
    //level+2위치에서 stmt본문을 출력한다.
}

class Let extends Stmt {
    // Let = Decls decls; Functions funs; Stmts stmts; // <- Disregard [Functions funs]
    Decls decls;
    Functions funs;
    Stmts stmts;

    Let(Decls ds, Stmts ss) {
        decls = ds;
		funs = null;
        stmts = ss;
    }

    Let(Decls ds, Functions fs, Stmts ss) {
        decls = ds;
	    funs = fs;
        stmts = ss;
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Let");
        Indent.display(level + 1, "Decls");
        decls.display(level + 2);
        Indent.display(level + 1, "Stmts");
        stmts.display(level + 2);
    }
    //Let문을 AST트리 형태로 표현해주는 역할
    //일단 현재 노드가 let블록 이라는 것을 출력한다.
    //이후에 level+1위치에서 Let블록 안의 변수 선언 부분임을 알려고
    //level+2위치에서 실제 변수 선언 부분 decl객체들의 리스트를 출력한다.
    //그런 다음 level+1위치에서 Let블록 안의 실행문 부분임을 알리고
    //level+2위치에서 실제 실행문 부분인 stmts객체들의 리스트를 출력한다.
}

class Read extends Stmt {
    // Read = Identifier id
    Identifier id;

    Read (Identifier v) {
        id = v;
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Read");
        id.display(level + 1);
    }
    //read문, 입출력 문장을 AST로 표현하는 부분
    //level단계 현재 노드가 read노드임을 알리고
    // level+1부분에 변수가 어떤 이름인지 출력한다.
}

class Print extends Stmt {
    // Print =  Expr expr
    Expr expr;

    Print (Expr e) {
        expr = e;
    }
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Print");
        expr.display(level + 1);
    }
    //print문, 출력문을 AST트리구조로 화면에 표현하는 부분
    //일단 print를 출력하여 현재 노드가 출력문임을 알린다.
    //print문 다음에 나오는 출력할 식인 expr을 level+1위치에 출력한다.
}

class Return extends Stmt {
    Identifier fid;
    Expr expr;

    Return (String s, Expr e) {
        fid = new Identifier(s);
        expr = e;
    }
}

class Try extends Stmt {
    // Try = Identifier id; Stmt stmt1; Stmt stmt2; 
    Identifier eid;
    Stmt stmt1; 
    Stmt stmt2; 

    Try(Identifier id, Stmt s1, Stmt s2) {
        eid = id; 
        stmt1 = s1;
        stmt2 = s2;
    }
}

class Raise extends Stmt {
    Identifier eid;

    Raise(Identifier id) {
        eid = id;
    }
}

class Exprs extends ArrayList<Expr> {
    // Exprs = Expr*
}

abstract class Expr extends Stmt {
    // Expr = Identifier | Value | Binary | Unary | Call

}

class Call extends Expr { 
    Identifier fid;  
    Exprs args;

    Call(Identifier id, Exprs a) {
       fid = id;
       args = a;
    }
}

class Identifier extends Expr {
    // Identifier = String id
    private String id;

    Identifier(String s) { id = s; }

    public String toString( ) { return id; }
    
    public boolean equals (Object obj) {
        String s = ((Identifier) obj).id;
        return id.equals(s);
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Identifier: " + id);
    }
    //Identifier 노드의 출력함수, 변수 이름을 AST트리 구조로 표현
    //현재 노드가 Identifier임을 알림과 동시에 뒤에 변수 이름을 출력한다.
}

class Array extends Expr {
    // Array = Identifier id; Expr expr
    Identifier id;
    Expr expr = null;

    Array(Identifier s, Expr e) {id = s; expr = e;}

    public String toString( ) { return id.toString(); }
    
    public boolean equals (Object obj) {
        String s = ((Array) obj).id.toString();
        return id.equals(s);
    }
}

class Value extends Expr {
    // Value = int | bool | string | array | function 
    protected boolean undef = true;
    Object value = null; // Type type;
    
    Value(Type t) {
        type = t;  
        if (type == Type.INT) value = new Integer(0);
        if (type == Type.BOOL) value = new Boolean(false);
        if (type == Type.STRING) value = "";
        undef = false;
    }

    Value(Object v) {
        if (v instanceof Integer) type = Type.INT;
        if (v instanceof Boolean) type = Type.BOOL;
        if (v instanceof String) type = Type.STRING;
        if (v instanceof Function) type = Type.FUN;
        if (v instanceof Value[]) type = Type.ARRAY;
        value = v; undef = false; 
    }

    Object value() { return value; }

    int intValue( ) { 
        if (value instanceof Integer) 
            return ((Integer) value).intValue(); 
        else return 0;
    }
    
    boolean boolValue( ) { 
        if (value instanceof Boolean) 
            return ((Boolean) value).booleanValue(); 
        else return false;
    } 

    String stringValue ( ) {
        if (value instanceof String) 
            return (String) value; 
        else return "";
    }

    Function funValue ( ) {
        if (value instanceof Function) 
            return (Function) value; 
        else return null;
    }

    Value[] arrValue ( ) {
        if (value instanceof Value[]) 
            return (Value[]) value; 
        else return null;
    }

    Type type ( ) { return type; }

    public String toString( ) {
        //if (undef) return "undef";
        if (type == Type.INT) return "" + intValue(); 
        if (type == Type.BOOL) return "" + boolValue();
	    if (type == Type.STRING) return "" + stringValue();
        if (type == Type.FUN) return "" + funValue();
        if (type == Type.ARRAY) return "" + arrValue();
        return "undef";
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Value: " + value);
    }
    //value, 상수 값을 ast트리구조로 출력하는 부분
    //현재 노드가 value임을 알리고 동시에 실제 value값을 출력하는 부분
}

class Binary extends Expr {
// Binary = Operator op; Expr expr1; Expr expr2;
    Operator op;
    Expr expr1, expr2;

    Binary (Operator o, Expr e1, Expr e2) {
        op = o; expr1 = e1; expr2 = e2;
    } // binary
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Binary");
        Indent.display(level + 1, "Operator: " + op);
        expr1.display(level + 1);
        expr2.display(level + 1);
    }
    //이항연산 노드를 화면에 출력하는 함수
    //현재 노드가 Binary노드임을 알리고
    //연산자를 먼저 출력한다. Operator를 출력하고 뒤에 실제 연산자를 출력한다.
    //level+1위치에 피연산자1을, 같은 위치에 피연산자2를 출력한다.
}

class Unary extends Expr {
    // Unary = Operator op; Expr expr
    Operator op;
    Expr expr;

    Unary (Operator o, Expr e) {
        op = o; //(o.val == "-") ? new Operator("neg"): o; 
        expr = e;
    } // unary
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Unary Operation: " + op);
        expr.display(level + 1);
    }
    //단항연산 노드를 화면에 출력하는 함수
    //현재 노드가 Unary Operation임을 알리고 뒤에 실제 연산자인 op를 출력한다.
    //level+1위치에 피연산자를 출력한다.
}

class Operator {
    String val;
    
    Operator (String s) { 
	val = s; 
    }

    public String toString( ) { 
	return val; 
    }

    public boolean equals(Object obj) { 
	return val.equals(obj); 
    }
    
    // TODO: [Insert the code of display()]
    public void display(int level) {
        Indent.display(level, "Operator: " + val);
    }
    //연산자 노드를 화면에 출력하는 함수
    //현재 노드가 Operator임을 알리고 뒤에 실제 연산자 기호인 val을 출력한다.
}