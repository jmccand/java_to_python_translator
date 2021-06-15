import java.util.List;
import java.util.ArrayList;

public class Node {

    /* POSSIBLE NODE TYPES:
       program
       comment
       newline
       class
       import
       constructor
       method
       attribute
       ANYTHING WITH BRACKETS
       call
       reassignment
       declaration
       space-call
       String
       char
       boolean
       int
       double
       variable
       I REALLY DON'T KNOW WHAT THIS IS
    */

    private String type;

    private int indent;
    
    private Node parent;
    private List<String> self;
    private List<Node> offspring = new ArrayList<Node>();

    private List<String> variables = new ArrayList<String>();

    public Node(String t, List<String> s, int i) {
        this.type = t;
        this.self = s;
	this.indent = i;
        this.recurse();
        this.trace(0);
        //System.out.println(this);
    }

    public Node(String t, Node p, List<String> s, int i) {
        this.type = t;
        this.parent = p;
        this.self = s;
	this.indent = i;
        this.recurse();
        //System.out.println(this);
    }

    public String getType() {
        return this.type;
    }

    public void recurse() {
        //System.out.println("recursing");
        int index = 0;
        switch (this.type) {
	case "program": {
	    while (index < self.size()) {
		if (self.get(index).length() >= 2) {
		    switch (self.get(index).substring(0, 2)) {
		    case "/*":
		    case "//": {
			this.offspring.add(new Node("comment", this, self.subList(index, index + 1), 0));
			index++;
			break;
		    }
		    default: {
			switch (self.get(index)) {
			case "import": {
			    this.offspring.add(new Node("import", this, self.subList(index, index + 3), 0));
			    index += 3;
			    break;
			}
			case "class": {
			    int startIndex = index - 1;
			    while (!self.get(index).equals("{")) {
				index++;
			    }
			    index++;
			    index = this.braces(index);
			    //System.out.println("\n\nFound the class:\n" + self.subList(startIndex, index));
			    this.offspring.add(new Node("class", this, self.subList(startIndex, index), 0));
			    break;
			}
			default: {
			    index++;
			}
			}
		    }
		    }
		}
		else if (self.get(index).equals("\n")) {
		    //this.offspring.add(new Node("newline", this, self.subList(index, index + 1)));
		    index++;
		}
		else {
		    index++;
		}
	    }
	    break;
	}
	case "class": {
	    while (!self.get(index).equals("{")) {
		index++;
	    }
	    index++;
	    while (index < self.size()) {
		if (self.get(index).equals("public") || self.get(index).equals("private")) {
		    if (self.get(index + 1).equals(self.get(2))) {
			int startIndex = index;
			while (!self.get(index).equals("{")) {
			    index++;
			}
			index++;
			index = this.braces(index);
			this.offspring.add(new Node("constructor", this, self.subList(startIndex, index), 1));
		    }
		    else {
			int isStatic = 0;
			if (self.get(index + 1).equals("static")) {
			    isStatic = 1;
			}
			if (self.get(index + isStatic + 3).equals("(")) {
			    int startIndex = index;
			    while (!self.get(index).equals("{")) {
				index++;
			    }
			    index++;
			    index = this.braces(index);
			    this.offspring.add(new Node("method", this, self.subList(startIndex, index), 1));
			}
			else {
			    int startIndex = index;
			    while (!self.get(index).equals(";")) {
				index++;
			    }
			    index++;
			    this.offspring.add(new Node("attribute", this, self.subList(startIndex, index), 1));
			}
		    }
		}
		else {
		    index++;
		}
	    }
	    break;
	}
	case "for":
	case "if":
	case "else if":
	case "while": {
	    while (self.get(index).equals("(") == false) {
		index++;
	    }
	    index++;
	    int startIndex = index;
	    int parentheses = 1;
	    while (parentheses > 0) {
		if (self.get(index).equals("(")) {
		    parentheses++;
		}
		else if (self.get(index).equals(")")) {
		    parentheses--;
		}
		index++;
	    }
	    if (this.type.equals("for") == false) {
		this.offspring.add(new Node("combination", this, self.subList(startIndex, index - 1), this.indent));
	    }
	    else {
		int semicolon1 = self.indexOf(";");
		int semicolon2 = self.subList(semicolon1 + 1, self.size()).indexOf(";") + semicolon1;
		this.offspring.add(new Node("declaration", this, self.subList(startIndex, semicolon1 + 1), this.indent));
		this.offspring.add(new Node("combination", this, self.subList(semicolon1 + 1, semicolon2 + 1), this.indent));
		this.offspring.add(new Node("reassignment", this, self.subList(semicolon2 + 2, index), this.indent));
		
	    }
	}
	case "constructor":
	case "method":
	case "else":
	case "do":
	case "try":
	case "case":
	case "default":
	case "catch": {
	    while (!self.get(index).equals("{")) {
		index++;
	    }
	    index++;
	    while (index < self.size() - 1) {
		int startLine = index;
		//System.out.println(self.get(startLine));
		while (!(self.get(index).equals("{") || self.get(index).equals("\n"))) {
		    index++;
		}
		if (self.get(index).equals("{")) {
		    index++;
		    index = this.braces(index);
		    if (self.get(startLine + 1).equals("if")) {
			this.offspring.add(new Node("else if", this, self.subList(startLine, index), this.indent + 1));
		    }
		    else {
			this.offspring.add(new Node(self.get(startLine), this, self.subList(startLine, index), this.indent + 1));
		    }
		}
		else {
		    if (startLine + 1 < index) {
			String[] declarations = {"=", "++", "--", "+=", "-=", "*=", "/="};
			String[] types = {"int", "boolean", "char", "String", "double"};
			String[] spaced_methods = {"return", "break"};
			if (in(declarations, self.get(startLine + 1))) {
			    this.offspring.add(new Node("reassignment", this, self.subList(startLine, index), this.indent + 1));
			}
			else if (self.get(startLine + 2).equals("=") || in(types, self.get(startLine))) {
			    this.offspring.add(new Node("declaration", this, self.subList(startLine, index), this.indent + 1));
			}
			else {
			    if (in(spaced_methods, self.get(startLine))) {
				this.offspring.add(new Node("space-call", this, self.subList(startLine, index), this.indent + 1));
			    }
			    else {
				this.offspring.add(new Node("call", this, self.subList(startLine, index), this.indent + 1));
			    }
			}
		    }
		    index++;
		    //this.offspring.add(new Node("newline", this, self.subList(index, index + 1)));
		}
	    }
	    break;
	}
	case "declaration": {
	    //System.out.println("declaration: " + self);
	    //System.out.println("declaration: " + self.subList(3, self.size()));
	    if (self.size() > 3) {
		this.offspring.add(this.subline(3, self.size() - 1));
	    }
	    this.addVariable(self.get(1));
	    break;
	}
	case "reassignment": {
	    //System.out.println("reassignment: " + self);
	    //System.out.println("reassignment: " + self.subList(2, self.size()));
	    if (self.size() > 3) {
		this.offspring.add(this.subline(2, self.size() - 1));
	    }
	    break;
	}
	case "call": {
	    //System.out.println(self);
	    while (!self.get(index).equals("(")) {
		index++;
	    }
	    int parentheses = 1;
	    int lastComma = index;
	    index++;
	    while (parentheses > 0) {
		if (self.get(index).equals(",")) {
		    //System.out.println("calling subline");
		    this.offspring.add(this.subline(lastComma + 1, index));
		    lastComma = index;
		    index++;
		}
		else {
		    if (self.get(index).equals("(")) {
			int internalParentheses = 1;
			index++;
			while (internalParentheses > 0) {
			    if (self.get(index).equals("(")) {
				internalParentheses++;
			    }
			    else if (self.get(index).equals(")")) {
				internalParentheses--;
			    }
			    index++;
			}
		    }
		    else if (self.get(index).equals(")")) {
			parentheses--;
			if (parentheses == 0 && lastComma + 1 < index) {
			    //System.out.println("calling subline");
			    this.offspring.add(this.subline(lastComma + 1, index));
			    lastComma = index;
			}
			index++;
		    }
		    else {
			index++;
		    }
		}
	    }
	    break;
	}
	case "combination": {
	    String[] combinations = {"+", "-", "*", "/", "%", "&&", "||", "==", "!=", "<", ">", ">=", "<="};
	    int lastSplit = index;
	    while (index < self.size()) {
		if (self.get(index).equals("(")) {
		    index++;
		    int parentheses = 1;
		    while (parentheses > 0) {
			if (index == self.size()) {
			    System.out.println("FOUND IT!!! " + self);
			}
			if (self.get(index).equals("(")) {
			    parentheses++;
			}
			else if (self.get(index).equals(")")) {
			    parentheses--;
			}
			index++;
		    }
		}
		else if (in(combinations, self.get(index))) {
		    this.offspring.add(this.subline(lastSplit, index));
		    index++;
		    lastSplit = index;
		}
		else {
		    index++;
		}
	    }
	    this.offspring.add(this.subline(lastSplit, index));
	    break;
	}
	case "typecast": {
	    while (!self.get(index).equals(")")) {
		index++;
	    }
	    index++;
	    this.offspring.add(this.subline(index, self.size()));
	    break;
	}
	case "attribute": {
	    String variable;
	    if (self.get(1).equals("static")) {
		variable = self.get(3);
	    }
	    else {
		variable = self.get(2);
	    }
	    this.addVariable(variable);
	    break;
	}
	case "switch": {
	    while (self.get(index).equals("(") == false) {
		index++;
	    }
	    index++;
	    int startIndex = index;
	    int parentheses = 1;
	    while (parentheses > 0) {
		if (self.get(index).equals("(")) {
		    parentheses++;
		}
		else if (self.get(index).equals(")")) {
		    parentheses--;
		}
		index++;
	    }
	    this.offspring.add(this.subline(startIndex, index - 1));
	    while (self.subList(index, self.size()).indexOf("case") != -1) {
		index += self.subList(index + 1, self.size()).indexOf("case") + 1;
		//System.out.println(index + " - " + self.get(index));
		int nextCase = self.subList(index + 1, self.size()).indexOf("case") + index + 1;
		//System.out.println("  " + nextCase + " - " + self.get(nextCase));
		if (nextCase != index) {
		    this.offspring.add(new Node("case", this, self.subList(index, nextCase), this.indent));
		}
		else {
		    nextCase = self.indexOf("default");
		    if (nextCase > index) {
			this.offspring.add(new Node("case", this, self.subList(index, nextCase), this.indent));
		    }
		    else {
			this.offspring.add(new Node("case", this, self.subList(index, self.size() - 1), this.indent));
		    }
		    index = self.size();
		}
	    }
	    if (self.indexOf("default") != -1) {
		this.offspring.add(new Node("default", this, self.subList(self.indexOf("default"), self.size()), this.indent));
	    }
	    break;
	}
	case "space-call": {
	    if (self.size() > 2) {
		this.offspring.add(this.subline(1, self.size() - 1));
	    }
	    break;
	}
	}
    }

    private Node subline(int from, int to) {
	//System.out.println("subline");
	String[] combinations = {"+", "-", "*", "/", "%", "&&", "||", "==", "!=", "<", ">", ">=", "<="};
	int index;
	boolean stripped = false;
	while (!stripped) {
	    if (!(self.get(from).equals("(") && self.get(to - 1).equals(")"))) {
		stripped = true;
	    }
	    else {
		index = from + 1;
		int parentheses = 1;
		while (parentheses > 0) {
		    if (self.get(index).equals("(")) {
			parentheses++;
		    }
		    else if (self.get(index).equals(")")) {
			parentheses--;
		    }
		    index++;
		}
		if (index == to) {
		    from++;
		    to--;
		}
		else {
		    stripped = true;
		}
	    }
	}
	if (from == to) {
	    System.out.println("FOUND IT!!! self is: " + self + "\nAnd the indexes are " + from + " and " + to);
	}
	if (from + 2 < to) {
	    index = from;
	    while (index < to) {
		if (self.get(index).equals("(")) {
		    index++;
		    int parentheses = 1;
		    while (parentheses > 0) {
			if (self.get(index).equals("(")) {
			    parentheses++;
			}
			else if (self.get(index).equals(")")) {
			    parentheses--;
			}
			index++;
		    }
		}
		else if (in(combinations, self.get(index))) {
		    //System.out.println(self);
		    return new Node("combination", this, self.subList(from, to), this.indent);
		}
		else {
		    index++;
		}
	    }
	    if (self.get(from).equals("new")) {
		return new Node("new", this, self.subList(from, to), this.indent);
	    }
	    else if (self.get(from).equals("(")) {
		String[] types = {"int", "boolean", "char", "String", "double"};
		if (in(types, self.get(from + 1))) {
		    return new Node("typecast", this, self.subList(from, to), this.indent);
		    }
	    }
	    else if (self.get(to - 1).equals(")")) {
		return new Node("call", this, self.subList(from, to), this.indent);
	    }
	}
	else if (from + 1 == to) {
	    //String[] types = {"int", "boolean", "char", "String", "double"};
	    if (self.get(from).substring(0, 1).equals("\"")) {
		return new Node("String", this, self.subList(from, to), this.indent);
	    }
	    else if (self.get(from).length() == 3 && self.get(from).substring(0, 1).equals("'")) {
		return new Node("char", this, self.subList(from, to), this.indent);
	    }
	    else if (self.get(from).equals("true") || self.get(from).equals("false")) {
		return new Node("boolean", this, self.subList(from, to), this.indent);
	    }
	    else if (isInt(self.get(from))) {
		return new Node("int", this, self.subList(from, to), this.indent);
	    }
	    else if (isDouble(self.get(from))) {
		return new Node("double", this, self.subList(from, to), this.indent);
	    }
	    else {
		return new Node("variable", this, self.subList(from, to), this.indent);
	    }
	}
	else {
	    //System.out.println("Unexpected entry of length 2: " + self.subList(from, to));
	}
	System.out.println(self.subList(from, to) + "   :   I REALLY DON'T KNOW WHAT THIS IS");
	return new Node("I REALLY DON'T KNOW WHAT THIS IS", this, self.subList(from, to), this.indent);
    }

    public void translate(List<String> translated) {
	switch (this.type) {
	case "program": {
	    //System.out.println("PROGRAM:\n" + self);
	    for (Node child : this.offspring) {
		child.translate(translated);
	    }
	    break;
	}
	case "comment": {
	    //System.out.println("COMMENT:\n" + self);
	    String pythonified = "";
	    String remaining = self.get(0).substring(2);
	    while (remaining.indexOf("\n") != -1) {
		pythonified += "#  ";
		pythonified += remaining.substring(0, remaining.indexOf("\n"));
		pythonified += "\n";
		remaining = remaining.substring(remaining.indexOf("\n") + 1);
	    }
	    if (remaining.length() > 2) {
		pythonified += remaining.substring(0, remaining.length() - 2);
	    }
	    translated.add(pythonified);
	    translated.add("\n");
	    break;
	}
	case "import": {
	    //System.out.println("IMPORT:\n" + self);
	    //switch (self.get(1)) {
	    //}
	    break;
	}
	case "class": {
	    //System.out.println("CLASS:\n" + self);
	    translated.add("\nclass ");
	    translated.add(self.get(2));
	    translated.add(":\n");
	    for (Node child : this.offspring) {
		child.translate(translated);
	    }
	    translated.add("\n");
	    break;
	}
	case "attribute": {
	    //System.out.println("ATTRIBUTE:\n" + self);
	    if (self.get(1).equals("static")) {
		translated.add("\n  " + self.get(3) + " = None\n");
	    }
	    break;
	}
	case "constructor": {
	    //System.out.println("CONSTRUCTOR:\n" + self);
	    translated.add("\n  def __init__(");
	    int index = 4;
	    if (!self.get(index).equals("{")) {
		translated.add(self.get(index));
		index += 3;
		while (!self.get(index - 2).equals(")")) {
		    translated.add(", ");
		    translated.add(self.get(index));
		    index += 3;
		}
	    }
	    translated.add("):\n");
	    for (Node child : this.offspring) {
		child.translate(translated);
	    }
	    break;
	}
	case "method": {
	    //System.out.println("METHOD:\n" + self);
	    int index;
	    if (self.get(1).equals("static")) {
		translated.add("\n  def " + self.get(3) + "(");
		index = 3;
	    }
	    else {
		translated.add("\n  def " + self.get(2) + "(self");
		index = 2;
	    }
	    index += 3;
	    if (!self.get(index).equals("{")) {
		translated.add(", " + self.get(index));
		index += 3;
		while (!self.get(index - 2).equals(")")) {
		    translated.add(", ");
		    translated.add(self.get(index));
		    index += 3;
		}
	    }
	    translated.add("):\n");
	    index = 0;
	    while (index < this.offspring.size()) {
		Node child = this.offspring.get(index);
		if (child.type.equals("do")) {
		    //System.out.println("do");
		    this.offspring.get(index + 1).translate(translated);
		    index++;
		}
		child.translate(translated);
		index++;
	    }
	    break;
	}
	case "reassignment": {
	    translated.add(this.doIndent());
	    switch (this.identify(self.get(0))) {
	    case 0: {
		translated.add("self." + self.get(0));
		break;
	    }
	    case 1: {
		translated.add(this.className() + "." + self.get(0));
		break;
	    }
	    case 2: {
		translated.add(self.get(0));
	    }
	    }
	    translated.add(" = ");
	    for (Node child : this.offspring) {
		child.translate(translated);
	    }
	    translated.add("\n");
	    break;
	}
	case "declaration": {
	    translated.add(this.doIndent());
	    translated.add(self.get(1) + " = ");
	    if (this.offspring.size() > 0) {
		for (Node child : this.offspring) {
		    child.translate(translated);
		}
	    }
	    else {
		translated.add("None");
	    }
	    translated.add("\n");
	    break;
	}
	case "new": {
	    switch (self.get(1)) {
	    default: {
		translated.add("'" + self.get(1) + "'");
		break;
	    }
	    }
	    break;
	}
	case "call": {
	    if (this.indent != this.parent.indent) {
		translated.add(doIndent());
	    }
	    switch (self.get(0)) {
	    case "while": {
		translated.add("first_while = True\n");
		translated.add(this.doIndent() + "while first_while or ");
		//System.out.println("while's offspring: " + this.offspring.size());
		for (Node child : offspring) {
		    child.translate(translated);
		}
		translated.add(":\n" + this.doIndent() + "  first_while = False");
		break;
	    }
	    case "System.out.print":
	    case "System.out.println": {
		translated.add("print(");
		break;
	    }
	    default: {
		String callto = self.get(0);
		if (callto.length() > 8 && callto.substring(callto.length() - 8, callto.length()).equals("nextInt")) {
		    translated.add("input(");
		}
		else {
		    translated.add(callto + "(");
		}
		break;
	    }
	    }
	    if (!self.get(0).equals("while") && this.offspring.size() > 0) {
		this.offspring.get(0).translate(translated);
		for (Node child : this.offspring.subList(1, this.offspring.size())) {
		    translated.add(", ");
		    child.translate(translated);
		}
	    }
	    if (self.get(0).equals("while") == false) {
		translated.add(")");
	    }
	    if (this.indent != this.parent.indent) {
		translated.add("\n");
	    }
	    break;
	}
	case "combination": {
	    //System.out.println("COMBINATION:\n" + self);
	    //System.out.println("offspring: " + offspring.size());
	    String[] combinations = {"+", "-", "*", "/", "%", "&&", "||", "==", "!=", "<", ">", ">=", "<="};
	    int index = 0;
	    for (Node child : this.offspring) {
		//new Throwable().printStackTrace(System.out);
		//System.out.println("BEFORE translated: " + translated.subList(translated.size() - 5, translated.size()));
		child.translate(translated);
		//System.out.println("AFTER translated: " + translated.subList(translated.size() - 5, translated.size()));
		index += child.self.size();
		if (index < self.size()) {
		    switch(self.get(index)) {
		    case "&&": {
			translated.add(" and ");
			break;
		    }
		    case "||": {
			translated.add(" or ");
			break;
		    }
		    default: {
			translated.add(" " + self.get(index) + " ");
			break;
		    }
		    }
		    index++;
		}
	    }	
	    break;
	}
	case "double":
	case "int": {
	    translated.add("" + self.get(0));
	    break;
	}
	case "String": {
	    translated.add("'" + self.get(0).substring(1, self.get(0).length() - 1) + "'");
	    break;
	}
	case "do": {
	    int index = 0;
	    while (index < this.offspring.size()) {
		Node child = this.offspring.get(index);
		if (this.offspring.get(index).type.equals("do")) {
		    this.offspring.get(index + 1).translate(translated);
		    index++;
		}
		child.translate(translated);
		index++;
	    }
	    break;
	}
	case "variable": {
	    switch (this.identify(self.get(0))) {
	    case 0: {
		translated.add("self." + self.get(0));
		break;
	    }
	    case 1: {
		translated.add(this.className() + "." + self.get(0));
		break;
	    }
	    case 2: {
		translated.add(self.get(0));
	    }
	    }
	    break;
	}
	case "if":
	case "else if": {
	    //System.out.println("IF:\n" + self);
	    translated.add(this.doIndent() + this.type + " ");
	    this.offspring.get(0).translate(translated);
	    translated.add(":\n");
	    for (Node child : offspring.subList(1, offspring.size())) {
		child.translate(translated);
	    }
	    break;
	}
	case "else": {
	    translated.add(this.doIndent() + "else:\n");
	    for (Node child : offspring) {
		child.translate(translated);
	    }
	    break;
	}
	case "for": {
	    translated.add(this.doIndent());
	    translated.add("for ");
	    translated.add(self.get(3));
	    translated.add(" in range(");
	    List<Node> thisOffspring = this.offspring;
	    thisOffspring.get(0).offspring.get(0).translate(translated);
	    translated.add(", ");
	    thisOffspring.get(1).offspring.get(0).translate(translated);
	    translated.add(", ");
	    //System.out.println("for loop 3rd offspring: " + thisOffspring.get(2).self);
	    switch (thisOffspring.get(2).self.get(1)) {
	    case "++": {
		translated.add("1");
		break;
	    }
	    case "--": {
		translated.add("-1");
		break;
	    }
	    }
	    translated.add("):\n");
	    for (Node child : offspring.subList(3, offspring.size())) {
		child.translate(translated);
	    }
	    break;
	}
	case "while": {
	    translated.add(this.doIndent() + "while ");
	    this.offspring.get(0).translate(translated);
	    translated.add(":\n");
	    for (Node child : offspring.subList(1, offspring.size())) {
		child.translate(translated);
	    }
	    break;
	}
	case "switch": {
	    //System.out.println("SWITCH:\n" + self);
	    translated.add(this.doIndent() + "switch_object = ");
	    this.offspring.get(0).translate(translated);
	    translated.add("\n");
	    for (Node child : offspring.subList(1, offspring.size())) {
		child.translate(translated);
	    }
	    break;
	}
	case "case": {
	    //System.out.println("CASE:\n" + self);
	    translated.add(this.doIndent());
	    if (this.parent.offspring.get(1) == this) {
		translated.add("if");
	    }
	    else {
		translated.add("elif");
	    }
	    translated.add(" switch_object == " + self.get(1) + ":\n");
	    for (Node child : offspring) {
		child.translate(translated);
	    }
	    break;
	}
	case "space-call": {
	    translated.add(this.doIndent());
	    switch (self.get(0)) {
	    case "return": {
		translated.add("return ");
		break;
	    }
	    }
	    for (Node child : offspring) {
		child.translate(translated);
	    }
	    translated.add("\n");
	    break;
	}
	case "typecast": {
	    if (this.indent != this.parent.indent) {
		translated.add(this.doIndent());
	    }
	    translated.add(self.get(1));
	    translated.add("(");
	    for (Node child : offspring) {
		child.translate(translated);
	    }
	    translated.add(")");
	    if (this.indent != this.parent.indent) {
		translated.add("\n");
	    }
	    break;
	}
	}
    }

    private String doIndent() {
	String total = "";
	for (int index = 0; index < this.indent; index++) {
	    total += "  ";
	}
	return total;
    }

    private void addVariable(String variable) {
	Node thisNode = this.parent;
	String[] storages = {"class", "constructor", "method", "for", "if", "else if", "else", "do", "while", "try", "catch"};
	while (!in(storages, thisNode.type)) {
	    thisNode = thisNode.parent;
	}
	if (thisNode.type.equals("class")) {
	    if (self.get(1).equals("static")) {
		thisNode.variables.add("static " + variable);
	    }
	    else {
		thisNode.variables.add("attribute " + variable);
	    }
	}
	else {
	    thisNode.variables.add(variable);
	}
    }

    private int identify(String variable) {
	Node thisNode = this.parent;
	while (thisNode.variables.indexOf(variable) == -1 && !thisNode.type.equals("class")) {
	    thisNode = thisNode.parent;
	}
	if (thisNode.type.equals("class")) {
	    if (thisNode.variables.indexOf("attribute " + variable) != -1) {
		return 0;
	    }
	    else if (thisNode.variables.indexOf("static " + variable) != -1) {
		return 1;
	    }
	    System.out.println("SKETCHY VARIABLE: " + variable);
	}
	return 2;
    }

    private String className() {
	Node thisNode = this.parent;
	while (!thisNode.type.equals("class")) {
	    thisNode = thisNode.parent;
	}
	return thisNode.self.get(2);
    }

    private int braces(int start) {
	int index = start;
	int count = 1;
	while (count > 0) {
	    if (self.get(index).equals("{")) {
		count++;
	    }
	    else if (self.get(index).equals("}")) {
		count--;
	    }
	    index++;
	}
	return index;
    }

    private static boolean isInt(String og) {
	int start = 0;
	if (og.substring(0, 1).equals("-")) {
	    start++;
	}
	for (int index = start; index < og.length(); index++) {
	    char thisChar = og.charAt(index);
	    int ascii = (int)thisChar;
	    if (!(ascii >= 48 && ascii <= 57)) {
		return false;
	    }
	}
	return true;
    }

    private static boolean isDouble(String og) {
	boolean period = false;
	int start = 0;
	if (og.substring(0, 1).equals("-")) {
	    start++;
	}
	for (int index = start; index < og.length(); index++) {
	    char thisChar = og.charAt(index);
	    int ascii = (int)thisChar;
	    if (!(ascii >= 48 && ascii <= 57)) {
		if (thisChar == '.' && !period) {
		    period = true;
		}
		else {
		    return false;
		}
	    }
	}
	return true;
    }

    private static boolean in(String[] full, String value) {
	//System.out.println("in - " + full[0]);
	for (String thisValue : full) {
	    if (thisValue.equals(value)) {
		return true;
	    }
	}
	return false;
    }

    public String toString() {
	String total = "";
	total += "type: " + this.type;
	if (this.parent != null) {
	    total += ", parent: " + this.parent.getType();
	}
	else {
	    total += ", parent: none";
	}
	total += ", contents:\n" + this.self.toString();
	total += "\noffspring: ";
	for (Node child : this.offspring) {
	    total += child.getType() + ", ";
	}
	return total;
    }

    public void trace(int generation) {
	String spaces = "";
	for (int repeat = 0; repeat < generation; repeat++) {
	    spaces += "  ";
	}
	System.out.print(spaces + this.type/* + "   " + self.get(0)*/);
	switch (this.type) {
	case "space-call":
	case "call": {
	    System.out.print(" - " + self.get(0));
	    break;
	}
	case "case": {
	    System.out.print(" - " + self.get(1));
	    break;
	}
	case "method": {
	    System.out.print(" - ");
	    if (self.get(1).equals("static")) {
		System.out.print(self.get(3));
	    }
	    else {
		System.out.print(self.get(2));
	    }
	    break;
	}
	}
	System.out.println();
	for (Node child : this.offspring) {
	    child.trace(generation + 1);
	}
    }
}
