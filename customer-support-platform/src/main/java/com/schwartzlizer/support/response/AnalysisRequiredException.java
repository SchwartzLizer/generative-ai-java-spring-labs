package com.schwartzlizer.support.response;

import com.schwartzlizer.support.common.InvalidStateTransitionException;

public class AnalysisRequiredException extends InvalidStateTransitionException { public AnalysisRequiredException() { super("Feedback must be analyzed before drafting a response"); } }
