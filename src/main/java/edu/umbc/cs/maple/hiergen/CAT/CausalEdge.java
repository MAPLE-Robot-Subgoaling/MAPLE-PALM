package edu.umbc.cs.maple.hiergen.CAT;

import java.util.Objects;

public class CausalEdge implements Comparable<CausalEdge> {

    protected int start;
    protected int end;
    protected String relevantVariable;

    public CausalEdge() {
        // for de/serialization
    }

    public CausalEdge(CausalEdge source) {
        this.start = source.start;
        this.end = source.end;
        this.relevantVariable = source.relevantVariable;
    }

    public CausalEdge(int start, int end, String variable) {
        this.start = start;
        this.end = end;
        this.relevantVariable = variable;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getRelevantVariable() {
        return relevantVariable;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setRelevantVariable(String relevantVariable) {
        this.relevantVariable = relevantVariable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CausalEdge that = (CausalEdge) o;
        return start == that.start &&
                end == that.end &&
                Objects.equals(relevantVariable, that.relevantVariable);
    }

    @Override
    public int hashCode() {

        return Objects.hash(start, end, relevantVariable);
    }

    @Override
    public String toString() {
        return "start=" + start + ", end=" + end + ", " + relevantVariable;
    }

    @Override
    public int compareTo(CausalEdge that) {
        int a = Integer.compare(this.end, that.end);
        a *= -1; // flip order
        if (a != 0) { return a; }
        int b = Integer.compare(this.start, that.start);
        b *= -1; // flip order
        if (b != 0) { return b; }
        int c = this.relevantVariable.compareTo(that.relevantVariable);
        return c;
    }
}
