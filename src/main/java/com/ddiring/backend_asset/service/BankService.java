package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.api.escrow.EscrowClient;
import com.ddiring.backend_asset.api.escrow.EscrowDto;
import com.ddiring.backend_asset.api.market.MarketDto;
import com.ddiring.backend_asset.api.product.ProductDto;
import com.ddiring.backend_asset.common.exception.BadParameter;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.entitiy.Bank;
import com.ddiring.backend_asset.entitiy.Escrow;
import com.ddiring.backend_asset.entitiy.History;
import com.ddiring.backend_asset.repository.BankRepository;
import com.ddiring.backend_asset.repository.EscrowRepository;
import com.ddiring.backend_asset.repository.HistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {
    private final BankRepository bankRepository;
    private final HistoryRepository historyRepository;
    private final EscrowRepository escrowRepository;
    private final EscrowClient escrowClient;

    @Transactional
    public BankSearchDto bankSearch(String userSeq, String role) {
        Optional<Bank> userId = bankRepository.findByUserSeqAndRole(userSeq, role);
        Bank bank = userId.orElseThrow(() -> new NotFound("계좌번호 없는데?"));

        BankSearchDto dto = new BankSearchDto(bank);
        // Bank 객체의 필드 값을 로그로 찍어봅니다.
        log.info("DB에서 조회한 Bank 객체 정보: {}, bankNumber={}, deposit={}, role={}",
                userSeq, bank.getBankNumber(), bank.getDeposit(), bank.getRole());
        // BankSearchDto 객체를 생성하고 로그를 찍습니다.
        log.info("BankSearchDto로 변환 완료: bankNumber={}, deposit={}",
                dto.getBankNumber(), dto.getDeposit());

        return new BankSearchDto(bank);
    }

    @Transactional
    public void createBank(String userSeq, String role) {
        Optional<Bank> existingBank = bankRepository.findByUserSeq(userSeq);
        if (existingBank.isPresent()) {
            throw new BadParameter("이미 계좌를 가지고 있습니다.");
        }
        Random random = new Random();
        int randomNumber = random.nextInt(90000) + 10000;

        String bankNumber;
        String bankNumber1;

        if (role.equals("USER") || role.equals("CREATOR")) {
            bankNumber = "02010-00-" + randomNumber;
            bankNumber1 = "02010-01-" + randomNumber;
            Optional<Bank> sameBankNumber = bankRepository.findByBankNumber(bankNumber);
            if (sameBankNumber.isPresent()) {
                throw new BadParameter("운 좋네 같은거 있음 다시 시도 해");
            }
            Bank bank = Bank.builder()
                    .userSeq(userSeq)
                    .role("USER")
                    .bankNumber(bankNumber)
                    .deposit(0)
                    .linkedAt(LocalDateTime.now())
                    .build();
            bankRepository.save(bank);

            Bank bank1 = Bank.builder()
                    .userSeq(userSeq)
                    .role("CREATOR")
                    .bankNumber(bankNumber1)
                    .deposit(0)
                    .linkedAt(LocalDateTime.now())
                    .build();
            bankRepository.save(bank1);

        } else {
            throw new BadParameter("유효하지 않은 role 값입니다.");
        }


    }
    @Transactional
    public void deposit(String userSeq, String role, DepositDto depositDto) {
        if (depositDto.getPrice() <= 0)
            throw new BadParameter("돈 넣어라");
        if (userSeq == null || role == null)
            throw new BadParameter("누구슈?");
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role).orElseThrow(() -> new NotFound("너 뭐냐"));
        bank.setDeposit(bank.getDeposit() + depositDto.getPrice());
        bankRepository.save(bank);

        History history = History.builder()
                .userSeq(userSeq)
                .role(role)
                .bankPrice(depositDto.getPrice())
                .moneyType(0)
                .bankTime(LocalDateTime.now())
                .build();
        historyRepository.save(history);

    }

    @Transactional
    public void withdrawal(String userSeq, String role, WithdrawalDto withdrawalDto) {
        if (withdrawalDto.getWithdrawal() <= 0)
            throw new BadParameter("장난하냐");
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role).orElseThrow(() -> new NotFound("너 뭐냐"));
        if (bank.getDeposit() - withdrawalDto.getWithdrawal() < 0) {
            throw new BadParameter("내 돈 빼먹지 마");
        }
        bank.setDeposit(bank.getDeposit() - withdrawalDto.getWithdrawal());

        bankRepository.save(bank);

        History history = History.builder()
                .userSeq(userSeq)
                .role(role)
                .bankPrice(withdrawalDto.getWithdrawal())
                .moneyType(1)
                .bankTime(LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }
    @Transactional
    public List<MoneyMoveDto> moneyMove(String userSeq, String role, Integer moneyType) {
        if (userSeq == null) {
            throw new NotFound("누구냐 넌");
        }
        List<History> history = historyRepository.findByUserSeqAndRoleAndMoneyTypeOrderByBankTimeDesc(userSeq, role, moneyType);
                return history.stream()
                        .map(MoneyMoveDto::new)
                        .collect(Collectors.toList());
    }

    @Transactional
    public List<MoneyMoveDto> allmoneyMove(String userSeq, String role) {
        if (userSeq == null) {
            throw new NotFound("누구냐 넌");
        }
        List<History> history = historyRepository.findByUserSeqAndRoleOrderByBankTimeDesc(userSeq, role);
        return history.stream()
                .map(MoneyMoveDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void escrowAccount(ProductDto productDto) {
        if (productDto.getAccount() == null) {
            throw new BadParameter("정보 줘");
        }
        if (productDto.getProjectId() == null) {
            throw new BadParameter("이제 다 됐다.");
        }
        Optional<Escrow> account = escrowRepository.findByProjectId(productDto.getProjectId());
        if (account.isPresent()) {
            throw new BadParameter("이미 있는뎌?");
        }

        Escrow escrow = Escrow.builder()
                .projectId(productDto.getProjectId())
                .account(productDto.getAccount())
                .build();
        escrowRepository.save(escrow);

    }

    @Transactional
    public Integer depositToEscrow(String userSeq, String role, MarketDto marketDto) {
        if (userSeq == null || marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("다시");
        }

        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("누구?"));


        if (bank.getDeposit() < marketDto.getPrice()) {
            throw new BadParameter("돈 없");
        }

        Escrow escrow = escrowRepository.findByProjectId(marketDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));



        return bank.getDeposit();
    }

    @Transactional
    public Integer withdrawalFromEscrow(String userSeq, String role, MarketDto marketDto) {
        if (userSeq == null || marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("다시");
        }

        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("누구?"));


        Escrow escrow = escrowRepository.findByProjectId(marketDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));

        bank.setDeposit(bank.getDeposit() + marketDto.getPrice());
        bankRepository.save(bank);

        return bank.getDeposit();
    }

    @Transactional
    public void setBuyPrice(String userSeq,String role, MarketBuyDto marketBuyDto) {
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("누구?"));
        Escrow escrow = escrowRepository.findByProjectId(marketBuyDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));
        bank.setDeposit(bank.getDeposit() - marketBuyDto.getBuyPrice());

        EscrowDto escrowDto = new EscrowDto();
        escrowDto.setAccount(escrow.getAccount());
        escrowDto.setUserSeq(userSeq);
        escrowDto.setTransSeq(marketBuyDto.getOrdersId());
        escrowDto.setTransType(1);
        escrowDto.setAmount(marketBuyDto.getBuyPrice());

        escrowClient.escrowDeposit(escrowDto);

        bankRepository.save(bank);
    }

    @Transactional
    public void escrowNumber(ProductDto productDto) {
        if (productDto.getAccount() == null) {
            throw new BadParameter("다시내놔");
        }
        Escrow escrow = Escrow.builder()
                .account(productDto.getAccount())
                .projectId(productDto.getProjectId())
                .build();
        escrowRepository.save(escrow);
    }
}
