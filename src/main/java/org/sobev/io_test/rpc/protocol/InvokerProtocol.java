package org.sobev.io_test.rpc.protocol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author luojx
 * @date 2022/5/19 17:13
 */
public class InvokerProtocol implements Serializable {
    private String className;

    private String methodName;

    private Class<?>[] paramTypes;

    private Object[] params;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "InvokerProtocol{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
