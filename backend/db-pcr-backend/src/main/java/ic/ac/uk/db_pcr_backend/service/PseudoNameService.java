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

    /**
     * Pre-create a pseudonym for the given assignment and realName.
     * This is used to pre-populate the database with pseudonyms
     * before the assignment is created in GitLab.
     */
    @Transactional
    public void createPseudoName(String assignmentUuid, String realName) {
        if (nameRepo.findByAssignmentUuidAndRealName(assignmentUuid, realName).isEmpty()) {
            // gather used pseudonyms for this assignment
            Set<String> used = nameRepo.findByAssignmentUuid(assignmentUuid).stream()
                    .map(PseudoNameEntity::getPseudoName)
                    .collect(Collectors.toSet());

            // build all adjective+animal combos
            List<String> candidates = new ArrayList<>();
            for (var adj : ADJECTIVES) {
                for (var ani : ANIMALS) {
                    candidates.add(adj + " " + ani);
                }
            }
            Collections.shuffle(candidates, rand);

            String pick = candidates.stream()
                    .filter(c -> !used.contains(c))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No pseudonyms left"));

            // persist the new pseudonym
            PseudoNameEntity e = new PseudoNameEntity(assignmentUuid, realName, pick);
            nameRepo.save(e);
        }
    }

    public void batchGenerateName(List<ReviewAssignmentEntity> assignments) {
        // 1️⃣ Fetch all existing pseudonyms for these assignment UUIDs
        Set<String> allAssignmentUuids = assignments.stream()
                .map(ReviewAssignmentEntity::getAssignmentUuid)
                .collect(Collectors.toSet());

        List<PseudoNameEntity> existing = nameRepo.findByAssignmentUuidIn(allAssignmentUuids);

        // 2️⃣ Build a map: (assignmentUuid, realName) → used pseudonyms
        Map<String, Set<String>> usedMap = new HashMap<>();
        for (var p : existing) {
            usedMap
                    .computeIfAbsent(p.getAssignmentUuid(), k -> new HashSet<>())
                    .add(p.getPseudoName());
        }

        // 3️⃣ Generate pseudonyms in memory
        List<PseudoNameEntity> toCreate = new ArrayList<>();
        for (var asn : assignments) {
            String uuid = asn.getAssignmentUuid();

            // ensure map entry
            usedMap.computeIfAbsent(uuid, k -> new HashSet<>());
            Set<String> used = usedMap.get(uuid);

            // generate for author if needed
            if (existing.stream().noneMatch(p -> p.getAssignmentUuid().equals(uuid)
                    && p.getRealName().equals(asn.getAuthorName()))) {

                String authorPseudo = pickPseudo(used);
                used.add(authorPseudo);
                toCreate.add(new PseudoNameEntity(uuid, asn.getAuthorName(), authorPseudo));
            }

            // generate for reviewer if needed
            if (existing.stream().noneMatch(p -> p.getAssignmentUuid().equals(uuid)
                    && p.getRealName().equals(asn.getReviewerName()))) {

                String reviewerPseudo = pickPseudo(used);
                used.add(reviewerPseudo);
                toCreate.add(new PseudoNameEntity(uuid, asn.getReviewerName(), reviewerPseudo));
            }
        }

        // 4️⃣ Batch‐save them in one shot
        nameRepo.saveAll(toCreate);
    }

    /** Picks a random adjective+animal not already in 'used' */
    private String pickPseudo(Set<String> used) {
        List<String> candidates = new ArrayList<>();
        for (var adj : ADJECTIVES) {
            for (var ani : ANIMALS) {
                candidates.add(adj + " " + ani);
            }
        }
        Collections.shuffle(candidates, rand);
        return candidates.stream()
                .filter(c -> !used.contains(c))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pseudonyms left"));
    }

}
