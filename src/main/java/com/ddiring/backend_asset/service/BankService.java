package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.api.market.MarketDto;
import com.ddiring.backend_asset.api.product.ProductDto;
import com.ddiring.backend_asset.common.exception.BadParameter;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.entitiy.Bank;
import com.ddiring.backend_asset.entitiy.Escrow;
import com.ddiring.backend_asset.entitiy.EscrowHistory;
import com.ddiring.backend_asset.entitiy.History;
import com.ddiring.backend_asset.repository.BankRepository;
import com.ddiring.backend_asset.repository.EscrowHistoryRepository;
import com.ddiring.backend_asset.repository.EscrowRepository;
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
    private final EscrowRepository escrowRepository;
    private final EscrowHistoryRepository escrowHistoryRepository;

    @Transactional
    public BankSearchDto bankSearch(Integer userId, Integer roll) {
        Optional<Bank> userid = bankRepository.findByUserSeqAndRoll(userId, roll);
        Bank bank = userid.orElseThrow(() -> new NotFound("계좌번호 없는데?"));
        return new BankSearchDto(bank);
    }

    @Transactional
    public void createBank(Integer userSeq, Integer roll, CreateBankDto createBankDto) {
        Optional<Bank> existingBank = bankRepository.findByUserSeq(userSeq);

        if (existingBank.isPresent()) {
            throw new BadParameter("이미 계좌를 가지고 있습니다.");
        }
        Random random = new Random();
        int randomNumber = random.nextInt(90000) + 10000;

        if (roll == 0) {
            String bankNumber1 = "02010-00-" + randomNumber;
            Optional<Bank> sameBankNumber = bankRepository.findByBankNumber(createBankDto.getBankNumber());
            if (sameBankNumber.equals("02010-00-" + randomNumber)) {
                throw new BadParameter("운 좋네 같은거 있음 다시 시도 해");
            }
            Bank bank = Bank.builder()
                    .userSeq(userSeq)
                    .roll(0)
                    .bankNumber(bankNumber1)
                    .deposit(0)
                    .linkedAt(LocalDate.now())
                    .build();
            bankRepository.save(bank);

        } else if (roll == 1) {
            String bankNumber2 = "02010-01-" + randomNumber;
            Optional<Bank> sameBankNumber = bankRepository.findByBankNumber(createBankDto.getBankNumber());
            if (sameBankNumber.equals("02010-01-" + randomNumber)) {
                throw new BadParameter("운 좋네 같은거 있음 다시 시도 해");
            }
            Bank bank1 = Bank.builder()
                    .userSeq(userSeq)
                    .roll(1)
                    .bankNumber(bankNumber2)
                    .deposit(0)
                    .linkedAt(LocalDate.now())
                    .build();
            bankRepository.save(bank1);
        }

    }
    @Transactional
    public void deposit(Integer userSeq, Integer roll, DepositDto depositDto) {
        if (depositDto.getDeposit() <= 0)
            throw new BadParameter("돈 넣어라");
        if (userSeq == null || roll == null)
            throw new BadParameter("누구슈?");
        Bank bank = bankRepository.findByUserSeqAndRoll(userSeq, roll).orElseThrow(() -> new NotFound("너 뭐냐"));
        bank.setDeposit(bank.getDeposit() + depositDto.getDeposit());
        bankRepository.save(bank);

        History history = History.builder()
                .userSeq(userSeq)
                .roll(roll)
                .bankPrice(depositDto.getDeposit())
                .moneyType(0)
                .bankTime(LocalDate.now())
                .build();
        historyRepository.save(history);

    }

    @Transactional
    public void withdrawal(Integer userSeq, Integer roll, WithdrawalDto withdrawalDto) {
        if (withdrawalDto.getWithdrawal() <= 0)
            throw new BadParameter("장난하냐");
        Bank bank = bankRepository.findByUserSeqAndRoll(withdrawalDto.getUserSeq(), withdrawalDto.getRoll()).orElseThrow(() -> new NotFound("너 뭐냐"));
        if (bank.getDeposit() - withdrawalDto.getWithdrawal() < 0) {
            throw new BadParameter("내 돈 빼먹지 마");
        }
        bank.setDeposit(bank.getDeposit() - withdrawalDto.getWithdrawal());

        bankRepository.save(bank);

        History history = History.builder()
                .userSeq(userSeq)
                .roll(roll)
                .bankPrice(withdrawalDto.getWithdrawal())
                .moneyType(1)
                .bankTime(LocalDate.now())
                .build();
        historyRepository.save(history);
    }
    @Transactional
    public List<MoneyMoveDto> moneyMove(Integer userSeq, Integer roll, Integer moneyType) {
        if (userSeq == null) {
            throw new NotFound("누구냐 넌");
        }
        List<History> history = historyRepository.findByUserSeqAndRollAndMoneyTypeOrderByBankTimeDesc(userSeq, roll, moneyType);
                return history.stream()
                        .map(MoneyMoveDto::new)
                        .collect(Collectors.toList());
    }

    @Transactional
    public List<MoneyMoveDto> allmoneyMove(Integer userSeq, Integer roll) {
        if (userSeq == null) {
            throw new NotFound("누구냐 넌");
        }
        List<History> history = historyRepository.findByUserSeqAndRollOrderByBankTimeDesc(userSeq, roll);
        return history.stream()
                .map(MoneyMoveDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void escrowAccount(ProductDto productDto) {
        if (productDto.getAccount() == null) {
            throw new BadParameter("정보 줘");
        }

        Optional<Escrow> account = escrowRepository.findByProjectId(productDto.getProjectId());
        if (account.isPresent()) {
            throw new BadParameter("이미 있는뎌?");
        }

        Escrow escrow = Escrow.builder()
                .account(productDto.getAccount())
                .title(productDto.getTitle())
                .projectId(productDto.getProjectId())
                .build();
        escrowRepository.save(escrow);

    }

    @Transactional
    public Integer depositToEscrow(MarketDto marketDto, ProductDto productDto ,Integer roll, Integer userSeq) {
        if (marketDto.getUserSeq() == null || marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("다시");
        }

        Bank bank = bankRepository.findByUserSeqAndRoll(userSeq, roll)
                .orElseThrow(() -> new NotFound("누구?"));


        if (bank.getDeposit() < marketDto.getPrice()) {
            throw new BadParameter("돈 없");
        }

        Escrow escrow = escrowRepository.findByProjectId(productDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));

        bank.setDeposit(bank.getDeposit() - marketDto.getPrice());
        bankRepository.save(bank);

        EscrowHistory escrowHistory = EscrowHistory.builder()
                .escrowAccount(escrow.getAccount())
                .userSeq(userSeq)
                .roll(roll)
                .price(marketDto.getPrice())
                .title(productDto.getTitle())
                .transferType(1)
                .transferDate(LocalDate.now())
                .build();

        escrowHistoryRepository.save(escrowHistory);

        return bank.getDeposit();
    }

    @Transactional
    public Integer withdrawalFromEscrow(MarketDto marketDto,ProductDto productDto, Integer roll, Integer userSeq) {
        if (marketDto.getUserSeq() == null || marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("다시");
        }

        Bank bank = bankRepository.findByUserSeqAndRoll(userSeq, roll)
                .orElseThrow(() -> new NotFound("누구?"));


        Escrow escrow = escrowRepository.findByProjectId(productDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));

        bank.setDeposit(bank.getDeposit() + marketDto.getPrice());
        bankRepository.save(bank);

        EscrowHistory escrowHistory = EscrowHistory.builder()
                .escrowAccount(escrow.getAccount())
                .userSeq(userSeq)
                .roll(roll)
                .price(marketDto.getPrice())
                .title(productDto.getTitle())
                .transferType(0)
                .transferDate(LocalDate.now())
                .build();

        escrowHistoryRepository.save(escrowHistory);

        return bank.getDeposit();
    }

    @Transactional
    public List<EscrowHistroyDto> escrowHistory(Integer userSeq, Integer roll, Integer trasferType) {
        List<EscrowHistory> escrowHistories = escrowHistoryRepository.findByUserSeqAndRollAndTransferTypeOrderByTransferDateDesc(userSeq, roll, trasferType);

        return escrowHistories.stream()
                .map(EscrowHistroyDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EscrowHistroyDto> escrowAllHistory(Integer userSeq, Integer roll) {
        List<EscrowHistory> escrowHistories = escrowHistoryRepository.findByUserSeqAndRollOrderByTransferDateDesc(userSeq, roll);

        return escrowHistories.stream()
                .map(EscrowHistroyDto::new)
                .collect(Collectors.toList());
    }


}
