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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private final EscrowHistoryRepository escrowHistoryRepository;

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

            Bank bank = Bank.builder()
                    .userSeq(userSeq)
                    .role("USER")
                    .bankNumber(bankNumber)
                    .deposit(0L)
                    .linkedAt(LocalDate.now())
                    .build();
            bankRepository.save(bank);

            Bank bank1 = Bank.builder()
                    .userSeq(userSeq)
                    .role("CREATOR")
                    .bankNumber(bankNumber1)
                    .deposit(0L)
                    .linkedAt(LocalDate.now())
                    .build();
            bankRepository.save(bank1);

        } else {
            throw new BadParameter("유효하지 않은 role 값입니다.");
        }

        Optional<Bank> sameBankNumber = bankRepository.findByBankNumber(bankNumber);
        if (sameBankNumber.isPresent()) {
            throw new BadParameter("운 좋네 같은거 있음 다시 시도 해");
        }

    }
    @Transactional
    public void deposit(String userSeq, String role, DepositDto depositDto) {
        if (depositDto.getDeposit() <= 0)
            throw new BadParameter("돈 넣어라");
        if (userSeq == null || role == null)
            throw new BadParameter("누구슈?");
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role).orElseThrow(() -> new NotFound("너 뭐냐"));
        bank.setDeposit(bank.getDeposit() + depositDto.getDeposit());
        bankRepository.save(bank);

        History history = History.builder()
                .userSeq(userSeq)
                .role(role)
                .bankPrice(depositDto.getDeposit())
                .moneyType(0)
                .bankTime(LocalDate.now())
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
                .bankTime(LocalDate.now())
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
                .title(productDto.getTitle())
                .build();
        escrowRepository.save(escrow);

    }

    @Transactional
    public Long depositToEscrow(MarketDto marketDto, ProductDto productDto ,String role, String userSeq) {
        if (marketDto.getUserSeq() == null || marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("다시");
        }

        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
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
                .role(role)
                .price(marketDto.getPrice())
                .title(productDto.getTitle())
                .transferType(1)
                .transferDate(LocalDate.now())
                .build();

        escrowHistoryRepository.save(escrowHistory);

        return bank.getDeposit();
    }

    @Transactional
    public Long withdrawalFromEscrow(MarketDto marketDto,ProductDto productDto, String role, String userSeq) {
        if (marketDto.getUserSeq() == null || marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("다시");
        }

        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("누구?"));


        Escrow escrow = escrowRepository.findByProjectId(productDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));

        bank.setDeposit(bank.getDeposit() + marketDto.getPrice());
        bankRepository.save(bank);

        EscrowHistory escrowHistory = EscrowHistory.builder()
                .escrowAccount(escrow.getAccount())
                .userSeq(userSeq)
                .role(role)
                .price(marketDto.getPrice())
                .title(productDto.getTitle())
                .transferType(0)
                .transferDate(LocalDate.now())
                .build();

        escrowHistoryRepository.save(escrowHistory);

        return bank.getDeposit();
    }

    @Transactional
    public List<EscrowHistroyDto> escrowHistory(String userSeq, String role, Integer trasferType) {
        List<EscrowHistory> escrowHistories = escrowHistoryRepository.findByUserSeqAndRoleAndTransferTypeOrderByTransferDateDesc(userSeq, role, trasferType);

        return escrowHistories.stream()
                .map(EscrowHistroyDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EscrowHistroyDto> escrowAllHistory(String userSeq, String role) {
        List<EscrowHistory> escrowHistories = escrowHistoryRepository.findByUserSeqAndRoleOrderByTransferDateDesc(userSeq, role);

        return escrowHistories.stream()
                .map(EscrowHistroyDto::new)
                .collect(Collectors.toList());
    }
    @Transactional
    public Long marketWithdrawal(String userSeq, String role, MarketDto marketDto) {
        if (marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("출금 금액은 0보다 커야 합니다.");
        }

        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("계좌번호 없는데?"));

        if (bank.getDeposit() < marketDto.getPrice()) {
            throw new BadParameter("내 돈 빼먹지 마");
        }

        bank.setDeposit(bank.getDeposit() - marketDto.getPrice());
        bankRepository.save(bank);

        return bank.getDeposit();
    }

    /**
     * 마켓 거래 후 남은 금액을 사용자 계좌에 입금합니다.
     * @param userSeq 사용자 시퀀스
     * @param role 사용자 역할
     * @param marketDto 입금 정보 (userSeq, price)
     * @return 입금 후 남은 잔액
     */
    @Transactional
    public Long marketDeposit(String userSeq, String role, MarketDto marketDto) {
        if (marketDto.getPrice() == null || marketDto.getPrice() <= 0) {
            throw new BadParameter("입금 금액은 0보다 커야 합니다.");
        }

        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("계좌번호 없는데?"));

        bank.setDeposit(bank.getDeposit() + marketDto.getPrice());
        bankRepository.save(bank);

        return bank.getDeposit();
    }

}
