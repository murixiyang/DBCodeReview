package ic.ac.uk.db_pcr_backend.service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.PseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.ProjectUserPseudonymRepo;
import ic.ac.uk.db_pcr_backend.repository.PseudonymRepo;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PseudoNameService {

    private static final List<String> ADJECTIVES = List.of(
            "Brave", "Clever", "Gentle", "Mighty", "Swift",
            "Radiant", "Curious", "Valiant", "Fierce", "Nimble",
            "Wise", "Bold", "Stoic", "Graceful", "Fearless",
            "Dashing", "Eloquent", "Luminous", "Steadfast", "Vibrant");
    private static final List<String> NOUNS = List.of(
            "Bear", "Frog", "Hawk", "Otter", "Panda",
            "Wolf", "Falcon", "Rabbit", "Fox", "Tiger",
            "Elephant", "Seal", "Giraffe", "Dolphin", "Eagle",
            "Koala", "Penguin", "Leopard", "Jaguar", "Badger",
            "Dragon", "Phoenix", "Unicorn", "Griffin", "Hydra");

    @Autowired
    private PseudonymRepo nameRepo;

    @Autowired
    private ProjectUserPseudonymRepo nameAssignmentRepo;

    @Autowired
    private GitLabService gitlabSvc;

    private final Random rand = new SecureRandom();

    /**
     * Returns an existing mapping, or creates a new pseudonym and mapping
     * for the given project, user, and role.
     */
    @Transactional
    public ProjectUserPseudonymEntity getOrCreatePseudoName(
            ProjectEntity groupProject, UserEntity user, RoleType role) {

        System.out.println("Service: PseudoNameService.getOrCreatePseudoName");

        return nameAssignmentRepo.findByGroupProjectAndUserAndRole(groupProject, user, role)
                .orElseGet(() -> createNameMapping(groupProject, user, role));
    }

    private ProjectUserPseudonymEntity createNameMapping(ProjectEntity groupProject,
            UserEntity user,
            RoleType role) {
        System.out.println("Service: PseudoNameService.createNameMapping");

        // collect used names in this project+role
        Set<String> used = new HashSet<>();
        nameAssignmentRepo.findByGroupProjectAndRole(groupProject, role)
                .forEach(pup -> used.add(pup.getPseudonym().getName()));

        // generate a unique name
        String name = generateUniqueName(used);

        // persist a new Pseudonym
        PseudonymEntity pseudo = new PseudonymEntity(role, name);
        nameRepo.save(pseudo);

        // map into project_user_pseudonyms
        ProjectUserPseudonymEntity nameAssignment = new ProjectUserPseudonymEntity(groupProject, user, role, pseudo);
        return nameAssignmentRepo.save(nameAssignment);
    }

    /**
     * Generate a pseudonym by combining adjective + animal,
     * adding a numeric suffix if needed to avoid collisions.
     */
    public String generateUniqueName(Set<String> used) {

        System.out.println("Service: PseudoNameService.generateUniqueName");

        int maxAttempts = 1000; // safety limit
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String base = ADJECTIVES.get(rand.nextInt(ADJECTIVES.size())) + " " +
                    NOUNS.get(rand.nextInt(NOUNS.size()));
            if (!used.contains(base)) {
                return base;
            }

            // Try with numbered suffix
            String candidate = generateUniqueNumberName(base);
            if (!used.contains(candidate)) {
                return candidate;
            }

        }
        throw new IllegalStateException("Unable to generate unique pseudonym after many attempts.");

    }

    public ProjectUserPseudonymEntity getPseudonymInReviewAssignment(ReviewAssignmentEntity assignment, RoleType role) {
        System.out.println("Service: PseudoNameService.getPseudonymInReviewAssignment");
        return nameAssignmentRepo
                .findByGroupProjectAndUserAndRole(assignment.getGroupProject(), assignment.getAuthor(), role)
                .orElseThrow(() -> new IllegalArgumentException("Unknown pseudonym for " + role + ": " + assignment));
    }

    public void savePseudonym(PseudonymEntity pseudoNym) {
        System.out.println("Service: PseudoNameService.savePseudonym");
        nameRepo.save(pseudoNym);
    }

    public String generateUniqueNumberName(String prefix) {
        int num = rand.nextInt(998) + 2; // from 2 to 999 inclusive
        return prefix + " #" + num;
    }

}
