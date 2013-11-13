package gov.census.torch;

public interface IModel {
    public double matchWeight(Record rec1, Record rec2);
}
