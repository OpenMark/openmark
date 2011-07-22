package om.tnavigator.auth;

import om.OmException;

public class AuthenticationInstantiationException extends OmException {

	private static final long serialVersionUID = 2571186728474324991L;

	public AuthenticationInstantiationException(String s) {
		super(s);
	}

	public AuthenticationInstantiationException(Exception x) {
		super(x);
	}

	public AuthenticationInstantiationException(String s, Exception x) {
		super(s, x);
	}

}
