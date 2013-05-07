package com.airbnb.chancery;

public class GithubFailure extends Exception {
	public GithubFailure(Throwable cause) {
		super(cause);
	}

	public static final class forDownload extends GithubFailure {
		public forDownload(Throwable cause) {
			super(cause);
		}
	}

	public static final class forRateLimit extends GithubFailure {
		public forRateLimit(Throwable cause) {
			super(cause);
		}
	}

	public static final class forReferenceCreation extends GithubFailure {
		public forReferenceCreation(Throwable cause) {
			super(cause);
		}
	}
}
