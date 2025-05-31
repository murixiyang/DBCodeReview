package ic.ac.uk.db_pcr_backend.service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.PseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.AuthorCodeEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.EvalReviewerEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.eval.AuthorCodeRepo;
import ic.ac.uk.db_pcr_backend.repository.eval.EvalReviewerRepo;

@Service
public class EvaluationService {

    @Autowired
    private EvalReviewerRepo evalReviewerRepo;

    @Autowired
    private AuthorCodeRepo authorCodeRepo;

    @Autowired
    private PseudoNameService pseudoNameSvc;

    private final Random random = new SecureRandom();

    /* Assign code to review for reviewer */
    @Transactional
    public EvalReviewerEntity getOrAssignEvalReviewer(UserEntity reviewer) {
        System.out.println("Service: EvaluationService.getOrAssignEvalReviewer");

        // We now assume 1 reviewer 1 assignment
        EvalReviewerEntity review = evalReviewerRepo.findByReviewer(reviewer)
                .orElseGet(() -> createEvalReviewAssignment(reviewer));

        return review;
    }

    private EvalReviewerEntity createEvalReviewAssignment(UserEntity reviewer) {
        System.out.println("Service: EvaluationService.createEvalReviewAssignment");

        // A pool of submissions that is not authored by the reviewer
        List<AuthorCodeEntity> authorCodePool = authorCodeRepo.findByAuthorNot(reviewer);

        // Randomly pick two distinct submissions
        Collections.shuffle(authorCodePool, random);
        AuthorCodeEntity firstCode = authorCodePool.get(0);
        AuthorCodeEntity secondCode = authorCodePool.get(1);

        // Randomize which round is anonymous
        boolean round1Anon = random.nextBoolean();
        boolean round2Anon = !round1Anon;

        // 6. Persist the assignment
        EvalReviewerEntity evalReview = new EvalReviewerEntity(reviewer, firstCode, round1Anon, secondCode, round2Anon,
                pseudoNameSvc.generateUniqueNumberName());

        return evalReviewerRepo.save(evalReview);
    }

}
