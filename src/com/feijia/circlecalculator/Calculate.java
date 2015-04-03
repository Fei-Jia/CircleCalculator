package com.feijia.circlecalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EmptyStackException;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

public class Calculate {
	public static DataBean calc(String eqa, int precision, boolean Radian){
		double result;
		boolean isError=false;
		StringBuffer resultstring = new StringBuffer();
		
		Operator factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {
	        @Override
	        public double apply(double... args) {
	            final int arg = (int) args[0];
	            if ((double) arg != args[0]) {
	                throw new IllegalArgumentException("Operand of factorial must be integer");
	            }
	            if (arg < 0) {
	                throw new IllegalArgumentException("Operand of factorial can't be negative");
	            }
	            double result = 1;
	                for (int i = 1; i <= arg; i++) {
	                result *= i;
	            }
	            return result;
	            }
	        };
		
		try{
			Expression e = new ExpressionBuilder(EquationParse.Parse(eqa, Radian))
			.variables("π", "e")
			.operator(factorial)
			.build()
			.setVariable("π", Math.PI)
			.setVariable("e", Math.E);
		    result = e.evaluate();
		    BigDecimal resultbd = new BigDecimal(result).setScale(precision, RoundingMode.HALF_UP);
		    result = resultbd.doubleValue();
		    resultstring.append(Double.toString(result));
		    isError=false;
		}
		catch(IllegalArgumentException|EmptyStackException ee){
		    resultstring.append(ee.getMessage());
		    isError=true;
		    if(ee.getMessage()==null) resultstring.append(" error!");
		}
		if(resultstring.charAt(resultstring.length()-2)=='.'&&resultstring.charAt(resultstring.length()-1)=='0')
			resultstring.delete(resultstring.length()-2, resultstring.length());
		return new DataBean(resultstring.toString(), isError);
	}
}