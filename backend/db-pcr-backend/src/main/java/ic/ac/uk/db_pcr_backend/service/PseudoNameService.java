package ic.ac.uk.db_pcr_backend.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.PseudoNameEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.repository.PseudoNameRepository;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PseudoNameService {

    private static final List<String> ADJECTIVES = List.of(
            "Brave", "Clever", "Gentle", "Mighty", "Swift");
    private static final List<String> ANIMALS = List.of(
            "Bear", "Frog", "Hawk", "Otter", "Panda");

    private final PseudoNameRepository nameRepo;
    private final Random rand = new SecureRandom();

    public PseudoNameService(PseudoNameRepository nameRepo) {
        this.nameRepo = nameRepo;
    }

    /**
     * Returns the existing pseudonym for this (assignment, realName),
     * or generates+stores a new one by picking from the adjective+animal lists
     * without colliding with existing pseudonyms in that assignment.
     */
    @Transactional
    public String getOrCreatePseudoName(String assignmentUuid, String realName) {
        return nameRepo.findByAssignmentUuidAndRealName(assignmentUuid, realName)
                .map(PseudoNameEntity::getPseudoName)
                .orElseGet(() -> {
                    // fetch used pseudonyms
                    Set<String> used = nameRepo.findByAssignmentUuid(assignmentUuid).stream()
                            .map(PseudoNameEntity::getPseudoName)
                            .collect(Collectors.toSet());

                    // build all combos
                    List<String> candidates = new ArrayList<>();
                    for (var adj : ADJECTIVES) {
                        for (var ani : ANIMALS) {
                            candidates.add(adj + " " + ani);
                        }
                    }

                    // pick a random unused one
                    Collections.shuffle(candidates, rand);
                    String pick = candidates.stream()
                            .filter(c -> !used.contains(c))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Ran out of pseudonyms"));

                    PseudoNameEntity p = new PseudoNameEntity(assignmentUuid, realName, pick);

                    nameRepo.save(p);
                    return pick;
                });
    }

}
