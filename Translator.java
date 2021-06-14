import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Translator {

    private static String[] twoCharExpressions = {"==", "!=", "+=", "-=", "*=", "/=", "<=", ">=", "||", "&&", "++", "--"};

    private File ogJava;
    private String newPython;
    private File pythonFile;

    public Translator(String input, String output) {
	this.ogJava = new File(input);
	this.pythonFile = new File(output);
	this.translate();
    }

    public File getJava() {
	return this.ogJava;
    }

    private void translate() {
	Scanner input;
	try {
	    input = new Scanner(ogJava);
	    List<String> lexed = new ArrayList<String>();
	    while (input.hasNextLine()) {
		String line = input.nextLine();
		List<String> lexedLine = lexer(line);
		for (String token : lexedLine) {
		    lexed.add(token);
		}
		lexed.add("\n");
	    }
	    lexed = clean(lexed);
	    Node program = new Node("program", lexed, 0);
	    List<String> translated = new ArrayList<String>();
	    program.translate(translated);
	    write(translated);
	}
	catch (FileNotFoundException ex) {
	    System.out.println("Cannot find " + ogJava + ", terminating the program");
	    System.exit(1);
	}
    }

    private static List lexer(String line) {
	List<String> lexed = new ArrayList<String>();
	int lineIndex = 0;
	while(lineIndex < line.length()) {
	    String next = lexNext(line, lineIndex);
	    lexed.add(next);
	    lineIndex += next.length();
	    //System.out.println("line index: " + lineIndex + "\n");
	}
	return lexed;
    }

    private static String lexNext(String line, int start) {
	int index = start + 1;
	String total = line.substring(start, start + 1);
	boolean evaluation = eval(total);
	//System.out.println("total: \"" + total + "\" - evaluation: " + evaluation);
	while(evaluation && index < line.length()) {
	    total += line.substring(index, index + 1);
	    index++;
	    evaluation = eval(total);
	    //System.out.println("total: \"" + total + "\" - evaluation: " + evaluation);
	}
	if ((!evaluation) && total.length() - 1 > 0) {
	    total = total.substring(0, total.length() - 1);
	}
        //System.out.println("\"" + total + "\"");
        if (total.equals("- ")) {
            System.out.println("FOUND the '- '!!!");
        }
	return total;
    }

    private static boolean eval(String total) {
	for (int charIndex = 0; charIndex < total.length(); charIndex++) {
	    switch(total.substring(charIndex, charIndex + 1)) {
	    case " ":
	    case ";":
	    case ")":
	    case "(":
	    case "\"":
	    case "=":
	    case ">":
	    case "<":
	    case "{":
	    case "}":
	    case ":":
	    case "+":
	    case "-":
	    case "*":
	    case "/":
	    case ",":
		return false;
	    }
	}
	return true;
    }

    private static List<String> clean(List<String> original) {
        //System.out.println(original);
	String larger = "";
	List<String> cleaned = new ArrayList<String>();
	int index = 0;
	while (index < original.size() - 1) {
	    if ((original.get(index) + original.get(index + 1)).equals("/*")) {
		larger += "/*";
		index += 2;
		while (!(original.get(index) + original.get(index + 1)).equals("*/")) {
		    larger += original.get(index);
		    index++;
		}
		larger += "*/";
		index += 2;
	    }
	    else if ((original.get(index) + original.get(index + 1)).equals("//")) {
		larger += "//";
		index += 2;
		while (!(original.get(index).equals("\n"))) {
		    larger += original.get(index);
		    index++;
		}
	    }
	    else if (original.get(index).equals("\"")) {
		larger += "\"";
		index ++;
		while (!(original.get(index)).equals("\"")) {
		    larger += original.get(index);
		    index++;
		}
		larger += "\"";
		index ++;
	    }
	    else if (in(twoCharExpressions, original.get(index) + original.get(index + 1))) {
		larger += (original.get(index) + original.get(index + 1));
		index += 2;
	    }
	    else if (original.get(index).equals("-") && isInt(original.get(index + 1))) {
		larger += "-" + original.get(index + 1);
		index += 2;
            }
	    else {
		larger = original.get(index);
		index++;
	    }
	    if (!larger.equals(" ")) {
		cleaned.add(larger);
	    }
	    larger = "";
	}
	if (index < original.size()) {
	    cleaned.add(original.get(index));
	}
        //System.out.println(cleaned);
	return cleaned;
    }

    private static boolean in(String[] full, String value) {
	for (String thisValue : full) {
	    if (thisValue.equals(value)) {
		return true;
	    }
	}
	return false;
    }

    private void write(List<String> source) {
	PrintWriter output;
	try {
	    output = new PrintWriter(pythonFile);
	    String newPython = "";
	    for (String token : source) {
		newPython += token;
		//System.out.print(token + ",");
	    }
	    output.print(newPython);
	    output.close();
	}
	catch (FileNotFoundException ex) {
	    System.out.println("Cannot find " + pythonFile + ", terminating the program");
	    System.exit(1);
	}
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
}
