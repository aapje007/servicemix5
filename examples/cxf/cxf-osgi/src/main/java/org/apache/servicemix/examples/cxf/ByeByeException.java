package org.apache.servicemix.examples.cxf;

import javax.xml.ws.WebFault;

@WebFault(name = "ByeByeName")
public class ByeByeException extends Exception {
}
