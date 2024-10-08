package kz.kazfintracker.sandboxserver.impl;

import dev.langchain4j.agent.tool.Tool;

public class BankingApiService {

    @Tool("Sums 2 given numbers")
    public double sum(double a, double b) {
        return a + b;
    }

    @Tool("Returns a square root of a given number")
    public double squareRoot(double x) {
        return Math.sqrt(x);
    }

}
