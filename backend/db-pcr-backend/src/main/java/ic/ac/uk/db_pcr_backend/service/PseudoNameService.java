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
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.ProjectUserPseudonymRepo;
import ic.ac.uk.db_pcr_backend.repository.PseudonymRepo;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PseudoNameService {

    private static final List<String> ADJECTIVES = List.of(
            "Brave", "Clever", "Gentle", "Mighty", "Swift");
    private static final List<String> ANIMALS = List.of(
            "Bear", "Frog", "Hawk", "Otter", "Panda");

    @Autowired
    private PseudonymRepo nameRepo;

    @Autowired
    private ProjectUserPseudonymRepo nameAssignmentRepo;

    private final Random rand = new SecureRandom();

    /**
     * Returns an existing mapping, or creates a new pseudonym and mapping
     * for the given project, user, and role.
     */
    @Transactional
    public ProjectUserPseudonymEntity getOrCreatePseudoName(
            ProjectEntity project, UserEntity user, RoleType role) {
        return nameAssignmentRepo.findByProjectAndUserAndRole(project, user, role)
                .orElseGet(() -> createNameMapping(project, user, role));
    }

    private ProjectUserPseudonymEntity createNameMapping(ProjectEntity project,
            UserEntity user,
            RoleType role) {

        // collect used names in this project+role
        Set<String> used = new HashSet<>();
        nameAssignmentRepo.findByProjectAndRole(project, role).forEach(pup -> used.add(pup.getPseudonym().getName()));

        // generate a unique name
        String name = generateUniqueName(used);

        // persist a new Pseudonym
        PseudonymEntity pseudo = new PseudonymEntity(role, name);
        nameRepo.save(pseudo);

        // map into project_user_pseudonyms
        ProjectUserPseudonymEntity nameAssignment = new ProjectUserPseudonymEntity(project, user, role, pseudo);
        return nameAssignmentRepo.save(nameAssignment);
    }

    /**
     * Generate a pseudonym by combining adjective + animal,
     * adding a numeric suffix if needed to avoid collisions.
     */
    private String generateUniqueName(Set<String> used) {
        int maxAttempts = ADJECTIVES.size() * ANIMALS.size() * 2;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String base = ADJECTIVES.get(rand.nextInt(ADJECTIVES.size()))
                    + " "
                    + ANIMALS.get(rand.nextInt(ANIMALS.size()));
            String candidate = base;
            if (used.contains(candidate)) {
                // add suffix
                candidate = base + (attempt + 1);
            }
            if (!used.contains(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique pseudonym after " + maxAttempts + " attempts");
    }

    // /**
    // * Returns the existing pseudonym for this (assignment, realName),
    // * or generates+stores a new one by picking from the adjective+animal lists
    // * without colliding with existing pseudonyms in that assignment.
    // */
    // @Transactional
    // public String getOrCreatePseudoName(String assignmentUuid, String realName) {
    // return nameRepo.findByAssignmentUuidAndRealName(assignmentUuid, realName)
    // .map(PseudoNameEntity::getPseudoName)
    // .orElseGet(() -> {
    // // fetch used pseudonyms
    // Set<String> used = nameRepo.findByAssignmentUuid(assignmentUuid).stream()
    // .map(PseudoNameEntity::getPseudoName)
    // .collect(Collectors.toSet());

    // // build all combos
    // List<String> candidates = new ArrayList<>();
    // for (var adj : ADJECTIVES) {
    // for (var ani : ANIMALS) {
    // candidates.add(adj + " " + ani);
    // }
    // }

    // // pick a random unused one
    // Collections.shuffle(candidates, rand);
    // String pick = candidates.stream()
    // .filter(c -> !used.contains(c))
    // .findFirst()
    // .orElseThrow(() -> new IllegalStateException("Ran out of pseudonyms"));

    // PseudoNameEntity p = new PseudoNameEntity(assignmentUuid, realName, pick);

    // System.out
    // .println("DBLOG: Create pseudonym " + pick + " for " + realName + " in " +
    // assignmentUuid);

    // nameRepo.save(p);
    // return pick;
    // });
    // }

}
