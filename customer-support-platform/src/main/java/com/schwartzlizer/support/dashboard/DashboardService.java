package com.schwartzlizer.support.dashboard;

import com.schwartzlizer.support.analysis.*;
import com.schwartzlizer.support.feedback.*;
import com.schwartzlizer.support.response.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Produces the read-only operational summary shown on the dashboard.
 *
 * <p>Issues one count query per enum constant of {@code FeedbackStatus}, {@code Sentiment},
 * {@code SupportCategory} and {@code Urgency}, plus a small number of aggregate counts, across three
 * repositories. All of it runs inside one read-only transaction so the counters are mutually consistent.
 */
@Service
public class DashboardService {
    private final FeedbackRepository feedbackRepository; private final FeedbackAnalysisRepository analysisRepository; private final ResponseDraftRepository draftRepository;
    public DashboardService(FeedbackRepository feedbackRepository, FeedbackAnalysisRepository analysisRepository, ResponseDraftRepository draftRepository) { this.feedbackRepository=feedbackRepository; this.analysisRepository=analysisRepository; this.draftRepository=draftRepository; }
    /**
     * Returns the current dashboard counters as a single consistent snapshot.
     *
     * <p>{@code open} counts feedback in {@code NEW}, {@code ANALYZED} or {@code IN_PROGRESS}; {@code urgent} is
     * the number of analyses with urgency {@code HIGH}; {@code pendingDrafts} is the number of drafts still in
     * decision {@code PENDING}. All counters are read in one read-only transaction, so they cannot disagree with
     * each other.
     *
     * @return a populated snapshot; every enum constant is present as a key, with 0 where there is no data
     */
    @Transactional(readOnly=true) public DashboardSummary summary() {
        EnumMap<FeedbackStatus,Long> statuses=new EnumMap<>(FeedbackStatus.class); for(var s:FeedbackStatus.values()) statuses.put(s,feedbackRepository.countByStatus(s));
        EnumMap<Sentiment,Long> sentiments=new EnumMap<>(Sentiment.class); for(var s:Sentiment.values()) sentiments.put(s,analysisRepository.countBySentiment(s));
        EnumMap<SupportCategory,Long> categories=new EnumMap<>(SupportCategory.class); for(var c:SupportCategory.values()) categories.put(c,analysisRepository.countByCategory(c));
        EnumMap<Urgency,Long> urgencies=new EnumMap<>(Urgency.class); for(var u:Urgency.values()) urgencies.put(u,analysisRepository.countByUrgency(u));
        long open=feedbackRepository.countByStatusIn(EnumSet.of(FeedbackStatus.NEW,FeedbackStatus.ANALYZED,FeedbackStatus.IN_PROGRESS));
        return new DashboardSummary(feedbackRepository.count(),open,urgencies.getOrDefault(Urgency.HIGH,0L),draftRepository.countByDecision(DraftDecision.PENDING),sentiments,categories,urgencies,statuses);
    }
}
