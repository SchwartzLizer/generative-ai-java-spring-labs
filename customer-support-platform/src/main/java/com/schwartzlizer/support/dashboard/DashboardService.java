package com.schwartzlizer.support.dashboard;

import com.schwartzlizer.support.analysis.*;
import com.schwartzlizer.support.feedback.*;
import com.schwartzlizer.support.response.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.EnumMap;
import java.util.EnumSet;

@Service
public class DashboardService {
    private final FeedbackRepository feedbackRepository; private final FeedbackAnalysisRepository analysisRepository; private final ResponseDraftRepository draftRepository;
    public DashboardService(FeedbackRepository feedbackRepository, FeedbackAnalysisRepository analysisRepository, ResponseDraftRepository draftRepository) { this.feedbackRepository=feedbackRepository; this.analysisRepository=analysisRepository; this.draftRepository=draftRepository; }
    @Transactional(readOnly=true) public DashboardSummary summary() {
        EnumMap<FeedbackStatus,Long> statuses=new EnumMap<>(FeedbackStatus.class); for(var s:FeedbackStatus.values()) statuses.put(s,feedbackRepository.countByStatus(s));
        EnumMap<Sentiment,Long> sentiments=new EnumMap<>(Sentiment.class); for(var s:Sentiment.values()) sentiments.put(s,analysisRepository.countBySentiment(s));
        EnumMap<SupportCategory,Long> categories=new EnumMap<>(SupportCategory.class); for(var c:SupportCategory.values()) categories.put(c,analysisRepository.countByCategory(c));
        EnumMap<Urgency,Long> urgencies=new EnumMap<>(Urgency.class); for(var u:Urgency.values()) urgencies.put(u,analysisRepository.countByUrgency(u));
        long open=feedbackRepository.countByStatusIn(EnumSet.of(FeedbackStatus.NEW,FeedbackStatus.ANALYZED,FeedbackStatus.IN_PROGRESS));
        return new DashboardSummary(feedbackRepository.count(),open,urgencies.getOrDefault(Urgency.HIGH,0L),draftRepository.countByDecision(DraftDecision.PENDING),sentiments,categories,urgencies,statuses);
    }
}
