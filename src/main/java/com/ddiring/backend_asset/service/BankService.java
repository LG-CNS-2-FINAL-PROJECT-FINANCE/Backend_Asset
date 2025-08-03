package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.common.exception.BadParameter;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.BankSearchDto;
import com.ddiring.backend_asset.dto.CreateBankDto;
import com.ddiring.backend_asset.dto.DepositDto;
import com.ddiring.backend_asset.dto.WithdrawalDto;
import com.ddiring.backend_asset.entitiy.Bank;
import com.ddiring.backend_asset.repository.BankRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BankService {
    private final BankRepository bankRepository;

    @Transactional
    public BankSearchDto bankSearch(Integer userSeq) {
        Optional<Bank> userid = bankRepository.findByUserSeq(userSeq);
        Bank bank = userid.orElseThrow(() -> new NotFound("계좌번호 없는데?"));
        return new BankSearchDto(bank);
    }

    @Transactional
    public void createBank(CreateBankDto createBankDto) {
        Random random = new Random();

        int randomNumber = random.nextInt(90000) + 10000;
        String bankNumber = "02003-01-" + randomNumber;
        Bank bank = Bank.builder()
                .userSeq(createBankDto.getUserSeq())
                .bankNumber(bankNumber)
                .deposit(0)
                .linkedAt(LocalDate.now())
                .build();
        bankRepository.save(bank);
    }
    @Transactional
    public void deposit(DepositDto depositDto) {
        if (depositDto.getDeposit() <= 0)
            throw new BadParameter("돈 넣어라");
        if (depositDto.getUserSeq() == null)
            throw new BadParameter("누구슈?");
        Optional<Bank> userid = bankRepository.findByUserSeq(depositDto.getUserSeq());
        Bank bank = userid.orElseThrow(() -> new NotFound("계좌번호 없는데?"));
        bank.setDeposit(bank.getDeposit() + depositDto.getDeposit());
        bankRepository.save(bank);
    }

    @Transactional
    public void withdrawal(WithdrawalDto withdrawalDto) {
        if (withdrawalDto.getWithdrawal() <= 0)
            throw new BadParameter("장난하냐");
        Optional<Bank> userid = bankRepository.findByUserSeq(withdrawalDto.getUserSeq());
        Bank bank = userid.orElseThrow(() -> new NotFound("계좌번호 없는데?"));
        if (bank.getDeposit() - withdrawalDto.getWithdrawal() < 0) {
            throw new BadParameter("내 돈 빼먹지 마");
        }
        bank.setDeposit(bank.getDeposit() - withdrawalDto.getWithdrawal());
        bankRepository.save(bank);
    }

}
