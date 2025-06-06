/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge8;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.lessons.challenges.Flags;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class Assignment8 implements AssignmentEndpoint {

  private static final Map<Integer, Integer> votes = new HashMap<>();

  static {
    votes.put(1, 400);
    votes.put(2, 120);
    votes.put(3, 140);
    votes.put(4, 150);
    votes.put(5, 300);
  }

  private final Flags flags;

  @GetMapping(value = "/challenge/8/vote/{stars}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<?> vote(
      @PathVariable(value = "stars") int nrOfStars, HttpServletRequest request) {
    // Simple implementation of VERB Based Authentication
    String msg = "";
    if (request.getMethod().equals("GET")) {
      var json =
          Map.of("error", true, "message", "Sorry but you need to login first in order to vote");
      return ResponseEntity.status(200).body(json);
    }
    Integer allVotesForStar = votes.getOrDefault(nrOfStars, 0);
    votes.put(nrOfStars, allVotesForStar + 1);
    return ResponseEntity.ok()
        .header("X-FlagController", "Thanks for voting, your flag is: " + flags.getFlag(8))
        .build();
  }

  @GetMapping("/challenge/8/votes/")
  public ResponseEntity<?> getVotes() {
    return ResponseEntity.ok(
        votes.entrySet().stream()
            .collect(Collectors.toMap(e -> "" + e.getKey(), e -> e.getValue())));
  }

  @GetMapping("/challenge/8/votes/average")
  public ResponseEntity<Map<String, Integer>> average() {
    int totalNumberOfVotes = votes.values().stream().mapToInt(i -> i.intValue()).sum();
    int categories =
        votes.entrySet().stream()
            .mapToInt(e -> e.getKey() * e.getValue())
            .reduce(0, (a, b) -> a + b);
    var json = Map.of("average", (int) Math.ceil((double) categories / totalNumberOfVotes));
    return ResponseEntity.ok(json);
  }

  @GetMapping("/challenge/8/notUsed")
  public AttackResult notUsed() {
    throw new IllegalStateException("Should never be called, challenge specific method");
  }
}
