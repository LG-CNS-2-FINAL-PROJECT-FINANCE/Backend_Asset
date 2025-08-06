package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.common.exception.BadParameter;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.entitiy.Bank;
import com.ddiring.backend_asset.entitiy.History;
import com.ddiring.backend_asset.repository.BankRepository;
import com.ddiring.backend_asset.repository.HistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankService {
    private final BankRepository bankRepository;
    private final HistoryRepository historyRepository;

    @Transactional
    public BankSearchDto bankSearch(Integer userId, Integer bankType) {
        Optional<Bank> userid = bankRepository.findByUserSeqAndBankType(userId, bankType);
        Bank bank = userid.orElseThrow(() -> new NotFound("계좌번호 없는데?"));
        return new BankSearchDto(bank);
    }

    @Transactional
    public void createBank(CreateBankDto createBankDto) {
        Optional<Bank> existingBank = bankRepository.findByUserSeq(createBankDto.getUserSeq());

        if (existingBank.isPresent()) {
            throw new BadParameter("이미 계좌를 가지고 있습니다.");
        }
        Random random = new Random();
        int randomNumber = random.nextInt(90000) + 10000;
        String bankNumber1 = "02010-00-" + randomNumber;
        Optional<Bank> sameBankNumber = bankRepository.findByBankNumber(createBankDto.getBankNumber());
        if (sameBankNumber.equals("02010-00-" + randomNumber)) {
            throw new BadParameter("운 좋네 같은거 있음 다시 시도 해");
        }
        Bank bank = Bank.builder()
                .userSeq(createBankDto.getUserSeq())
                .bankType(0)
                .bankNumber(bankNumber1)
                .deposit(0)
                .linkedAt(LocalDate.now())
                .build();
        bankRepository.save(bank);
        String bankNumber2 = "02010-01-" + randomNumber;
        Bank bank1 = Bank.builder()
                .userSeq(createBankDto.getUserSeq())
                .bankType(1)
                .bankNumber(bankNumber2)
                .deposit(0)
                .linkedAt(LocalDate.now())
                .build();
        bankRepository.save(bank1);

    }
    @Transactional
    public void deposit(DepositDto depositDto) {
        if (depositDto.getDeposit() <= 0)
            throw new BadParameter("돈 넣어라");
        if (depositDto.getUserSeq() == null || depositDto.getBankType() == null)
            throw new BadParameter("누구슈?");
        Bank bank = bankRepository.findByUserSeqAndBankType(depositDto.getUserSeq(), depositDto.getBankType()).orElseThrow(() -> new NotFound("너 뭐냐"));
        bank.setDeposit(bank.getDeposit() + depositDto.getDeposit());
        bankRepository.save(bank);

        History history = History.builder()
                .userSeq(depositDto.getUserSeq())
                .bankType(depositDto.getBankType())
                .bankPrice(depositDto.getDeposit())
                .moneyType(0)
                .bankTime(LocalDate.now())
                .build();
        historyRepository.save(history);

    }

    @Transactional
    public void withdrawal(WithdrawalDto withdrawalDto) {
        if (withdrawalDto.getWithdrawal() <= 0)
            throw new BadParameter("장난하냐");
        Bank bank = bankRepository.findByUserSeqAndBankType(withdrawalDto.getUserSeq(), withdrawalDto.getBankType()).orElseThrow(() -> new NotFound("너 뭐냐"));
        if (bank.getDeposit() - withdrawalDto.getWithdrawal() < 0) {
            throw new BadParameter("내 돈 빼먹지 마");
        }
        bank.setDeposit(bank.getDeposit() - withdrawalDto.getWithdrawal());

        bankRepository.save(bank);

        History history = History.builder()
                .userSeq(withdrawalDto.getUserSeq())
                .bankType(withdrawalDto.getBankType())
                .bankPrice(withdrawalDto.getWithdrawal())
                .moneyType(1)
                .bankTime(LocalDate.now())
                .build();
        historyRepository.save(history);
    }
    @Transactional
    public List<MoneyMoveDto> moneyMove(Integer userSeq, Integer bankType, Integer moneyType) {
        if (userSeq == null) {
            throw new NotFound("누구냐 넌");
        }
        List<History> history = historyRepository.findByUserSeqAndBankTypeAndMoneyTypeOrderByBankTimeDesc(userSeq, bankType, moneyType);
                return history.stream()
                        .map(MoneyMoveDto::new)
                        .collect(Collectors.toList());
    }

    @Transactional
    public List<MoneyMoveDto> allmoneyMove(Integer userSeq, Integer bankType) {
        if (userSeq == null) {
            throw new NotFound("누구냐 넌");
        }
        List<History> history = historyRepository.findByUserSeqAndBankTypeOrderByBankTimeDesc(userSeq, bankType);
        return history.stream()
                .map(MoneyMoveDto::new)
                .collect(Collectors.toList());
    }
}
