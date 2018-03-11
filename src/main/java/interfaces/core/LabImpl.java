package interfaces.core;

import interfaces.pipeline.Result;

public class LabImpl implements Lab {

    @Override
    public void publish(Result data) {
        System.out.println(data.getStringRepresentation());
    }
}
