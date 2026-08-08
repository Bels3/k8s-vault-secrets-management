package com.insurance.policy.service;

import com.insurance.policy.model.Policy;
import com.insurance.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    public List<Policy> getAllPolicies() {
        log.info("Fetching all policies");
        return policyRepository.findAll();
    }

    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
    }

    public Policy getPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyNumber));
    }

    @Transactional
    public Policy createPolicy(Policy policy) {
        log.info("Creating policy for holder: {}", policy.getHolderName());
        return policyRepository.save(policy);
    }

    @Transactional
    public Policy updateStatus(Long id, Policy.PolicyStatus status) {
        Policy policy = getPolicyById(id);
        policy.setStatus(status);
        log.info("Updated policy {} status to {}", policy.getPolicyNumber(), status);
        return policyRepository.save(policy);
    }

    @Transactional
    public void deletePolicy(Long id) {
        policyRepository.deleteById(id);
        log.info("Deleted policy with id: {}", id);
    }
}
