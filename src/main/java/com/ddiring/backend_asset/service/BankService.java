package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.api.escrow.EscrowClient;
import com.ddiring.backend_asset.api.escrow.EscrowDto;
import com.ddiring.backend_asset.api.market.MarketDto;
import com.ddiring.backend_asset.api.product.DistributionDto;
import com.ddiring.backend_asset.api.product.ProductDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.common.exception.BadParameter;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.entitiy.Bank;
import com.ddiring.backend_asset.entitiy.Escrow;
import com.ddiring.backend_asset.entitiy.History;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.repository.BankRepository;
import com.ddiring.backend_asset.repository.EscrowRepository;
import com.ddiring.backend_asset.repository.HistoryRepository;
import com.ddiring.backend_asset.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TokenRepository tokenRepository;
    private final EscrowRepository escrowRepository;
    private final EscrowClient escrowClient;

    // 내 계좌 조회
    @Transactional(readOnly = true)
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

    // 계좌 생성
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

    // 내 계좌에 내가 입금
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

    // 내 계좌에 내가 출금
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

    @Transactional(readOnly = true)
    public List<MoneyMoveDto> moneyMove(String userSeq, String role, Integer moneyType) {
        if (userSeq == null) {
            throw new NotFound("누구냐 넌");
        }
        List<History> history = historyRepository.findByUserSeqAndRoleAndMoneyTypeOrderByBankTimeDesc(userSeq, role,
                moneyType);
        return history.stream()
                .map(MoneyMoveDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MoneyMoveDto> allMoneyMove(String userSeq, String role) {
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
            Escrow escrow = account.get();
            escrow.setTitle(productDto.getTitle());
            escrowRepository.save(escrow);
        } else {
            Escrow escrow = Escrow.builder()
                    .title(productDto.getTitle())
                    .projectId(productDto.getProjectId())
                    .account(productDto.getAccount())
                    .build();
            escrowRepository.save(escrow);
        }
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

        EscrowDto escrowDto = EscrowDto.builder()
                .account(escrow.getAccount())
                .userSeq(userSeq)
                .transSeq(Integer.valueOf(marketDto.getMarketId()))
                .transType(5)
                .amount(marketDto.getPrice())
                .build();

        escrowClient.escrowWithdrawal(escrowDto);

        bank.setDeposit(bank.getDeposit() + marketDto.getPrice());
        bankRepository.save(bank);

        return bank.getDeposit();
    }

    @Transactional
    public void setBuyPrice(String userSeq, String role, MarketBuyDto marketBuyDto) {
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("누구?"));
        Escrow escrow = escrowRepository.findByProjectId(marketBuyDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));
        if (bank.getDeposit() < marketBuyDto.getBuyPrice() + (int) (marketBuyDto.getBuyPrice() * 0.03)) {
            throw new BadParameter("돈없어 그만");
        }
        if (marketBuyDto.getTransType() == 0) {
            EscrowDto escrowDto = new EscrowDto();
            escrowDto.setAccount(escrow.getAccount());
            escrowDto.setUserSeq(userSeq);
            escrowDto.setTransSeq(marketBuyDto.getOrdersId());
            escrowDto.setTransType(marketBuyDto.getTransType());
            escrowDto.setAmount((marketBuyDto.getBuyPrice()));

            escrowClient.escrowDeposit(escrowDto);

            bank.setDeposit((bank.getDeposit() - marketBuyDto.getBuyPrice()));

            bankRepository.save(bank);
        } else {
            EscrowDto escrowDto = new EscrowDto();
            escrowDto.setAccount(escrow.getAccount());
            escrowDto.setUserSeq(userSeq);
            escrowDto.setTransSeq(marketBuyDto.getOrdersId());
            escrowDto.setTransType(marketBuyDto.getTransType());
            escrowDto.setAmount((int) (marketBuyDto.getBuyPrice() + (marketBuyDto.getBuyPrice() * 0.03)));

            escrowClient.escrowDeposit(escrowDto);

            bank.setDeposit(
                    (int) (bank.getDeposit() - (marketBuyDto.getBuyPrice() + (marketBuyDto.getBuyPrice() * 0.03))));

            bankRepository.save(bank);
        }
    }

    @Transactional
    public void setRefundToken(String userSeq, String role, MarketRefundDto marketRefundDto) {

        if (marketRefundDto.getOrderType() == 0) {

            Token token = tokenRepository.findByUserSeqAndProjectId(userSeq, marketRefundDto.getProjectId())
                    .orElseThrow(() -> new NotFound("누구?"));

            token.setAmount(token.getAmount() + marketRefundDto.getRefundAmount());
            tokenRepository.save(token);

        } else if (marketRefundDto.getOrderType() == 1) {
            Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                    .orElseThrow(() -> new NotFound("누구?"));
            Escrow escrow = escrowRepository.findByProjectId(marketRefundDto.getProjectId())
                    .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));
            EscrowDto escrowDto = new EscrowDto();
            escrowDto.setAccount(escrow.getAccount());
            escrowDto.setUserSeq(userSeq);
            escrowDto.setTransSeq(marketRefundDto.getOrdersId());
            escrowDto.setTransType(-1);
            escrowDto.setAmount((int) (marketRefundDto.getRefundPrice() + (marketRefundDto.getRefundPrice() * 0.03)));

            escrowClient.escrowDeposit(escrowDto);
            bank.setDeposit((int) (bank.getDeposit() + marketRefundDto.getRefundPrice()
                    + (marketRefundDto.getRefundPrice() * 0.03)));
        } else if (marketRefundDto.getOrderType() == 2) {
            Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                    .orElseThrow(() -> new NotFound("누구?"));
            Escrow escrow = escrowRepository.findByProjectId(marketRefundDto.getProjectId())
                    .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));

            EscrowDto escrowDto = new EscrowDto();
            escrowDto.setAccount(escrow.getAccount());
            escrowDto.setUserSeq(userSeq);
            escrowDto.setTransSeq(marketRefundDto.getOrdersId());
            escrowDto.setTransType(-1);
            escrowDto.setAmount((marketRefundDto.getRefundPrice()));

            escrowClient.escrowWithdrawal(escrowDto);

            bank.setDeposit(bank.getDeposit() + marketRefundDto.getRefundPrice());
            bankRepository.save(bank);
        }

    }

    @Transactional
    public String getMarketTitleDto(String projectId) {
        Escrow escrow = escrowRepository.findByProjectId(projectId)
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));
        return escrow.getTitle();
    }

    @Transactional
    public void depositForTrade(String userSeq, String role, int amount) {
        if (amount <= 0) {
            throw new BadParameter("입금 금액은 0보다 커야 합니다.");
        }
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("판매자의 은행 계좌를 찾을 수 없습니다. userSeq: " + userSeq));

        bank.setDeposit((int) (bank.getDeposit() + (int) amount));
        bankRepository.save(bank);

    }

    @Transactional
    public void setprofit(String userSeq, String role, MarketBuyDto marketBuyDto) {
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("누구?"));
        Escrow escrow = escrowRepository.findByProjectId(marketBuyDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));
        if (bank.getDeposit() < marketBuyDto.getBuyPrice()) {
            throw new BadParameter("돈없어 그만");
        }

        escrow.setDistribution(marketBuyDto.getBuyPrice());
        bank.setDeposit(bank.getDeposit() - marketBuyDto.getBuyPrice());
        bankRepository.save(bank);

        EscrowDto escrowDto = new EscrowDto();
        escrowDto.setAccount(escrow.getAccount());
        escrowDto.setUserSeq(userSeq);
        escrowDto.setTransSeq(marketBuyDto.getOrdersId());
        escrowDto.setTransType(4);
        escrowDto.setAmount(marketBuyDto.getBuyPrice());

        escrowClient.escrowDeposit(escrowDto);
    }

    @Transactional
    public Integer getAllMoney(String userSeq, String role, AssetAllMoneyDto assetAllMoneyDto) {
        Integer money = 0;
        List<History> histories = historyRepository.findByUserSeqAndRoleAndMoneyTypeOrderByBankTimeDesc(userSeq, role,
                assetAllMoneyDto.getMoneyType());
        for (History history : histories) {
            money = money + history.getBankPrice();
        }
        return money;
    }

    @Transactional
    public void getDistribution(DistributionDto distributionDto) {
        Escrow escrow = escrowRepository.findByProjectId(distributionDto.getProjectId())
                .orElseThrow(() -> new NotFound("프로젝트의 에스크로 계좌 으디있냐"));

        escrow.setDistribution(distributionDto.getDistributionAmount());
        escrowRepository.save(escrow);
    }

    @Transactional(readOnly = true)
    public boolean checkUserBalance(String userSeq, String role, Integer price) {
        Bank bank = bankRepository.findByUserSeqAndRole(userSeq, role)
                .orElseThrow(() -> new NotFound("사용자 계좌를 찾을 수 없습니다."));
        return bank.getDeposit() >= (price + (price * 0.03));
    }

//    @Transactional
//    public void divideMoney(String userSeq, String role, MarketBuyDto marketBuyDto) {
//        Integer perPrice = 0;
//        Integer allAmount = 0;
//
//        Escrow escrow = escrowRepository.findByProjectId(marketBuyDto.getProjectId())
//                .orElseThrow(() -> new NotFound("없"));
//
//        List<Token> token = tokenRepository.findByProjectId(marketBuyDto.getProjectId());
//        for (Token token1 : token) {
//            allAmount = allAmount + token1.getAmount();
//        }
//        perPrice = marketBuyDto.getBuyPrice() / allAmount;
//
//        for (Token token2 : token) {
//            Bank bank = bankRepository.findByUserSeqAndRole(token2.getUserSeq(), "USER").orElseThrow(() -> new NotFound("음슴"));
//            EscrowDto escrowDto = EscrowDto.builder()
//                    .account(escrow.getAccount())
//                    .transSeq(12345)
//                    .userSeq(userSeq)
//                    .transType(marketBuyDto.getTransType())
//                    .amount(bank.getDeposit() + (token2.getAmount() * perPrice))
//                    .build();
//
//            escrowClient.escrowWithdrawal(escrowDto);
//
//            bank.setDeposit(bank.getDeposit() + (token2.getAmount() * perPrice));
//            bankRepository.save(bank);
//
//        }
//    }

    @Transactional
    public void divideMoney(String userSeq, String role, MarketBuyDto marketBuyDto) {
        // Integer perPrice = 0;
        // Integer allAmount = 0;

        // Escrow escrow = escrowRepository.findByProjectId(marketBuyDto.getProjectId())
        //         .orElseThrow(() -> new NotFound("없"));

        // List<Token> token = tokenRepository.findByProjectId(marketBuyDto.getProjectId());
        // for (Token token1 : token) {
        //     allAmount = allAmount + token1.getAmount();
        // }
        // perPrice = marketBuyDto.getBuyPrice() / allAmount;

        // for (Token token2 : token) {
        //     Bank bank = bankRepository.findByUserSeqAndRole(token2.getUserSeq(), "USER")
        //             .orElseThrow(() -> new NotFound("음슴"));
        //     EscrowDto escrowDto = EscrowDto.builder()
        //             .account(escrow.getAccount())
        //             .transSeq(12345)
        //             .userSeq(userSeq)
        //             .transType(3)
        //             .amount(bank.getDeposit() + (token2.getAmount() * perPrice))
        //             .build();

        //     escrowClient.escrowWithdrawal(escrowDto);

        //     bank.setDeposit(bank.getDeposit() + (token2.getAmount() * perPrice));
        //     bankRepository.save(bank);

        // }
        Integer allAmount = tokenRepository.findAllByProjectId(marketBuyDto.getProjectId())
        .stream()
        .mapToInt(Token::getAmount)
        .sum();

        if (allAmount == 0) {
            throw new IllegalStateException("토큰 총액이 0입니다.");
        }

        Escrow escrow = escrowRepository.findByProjectId(marketBuyDto.getProjectId())
                .orElseThrow(() -> new NotFound("에스크로 계좌 없음"));

        List<Token> tokens = tokenRepository.findAllByProjectId(marketBuyDto.getProjectId());

        for (Token t : tokens) {
            double divideRate = (double) t.getAmount() / allAmount;
            int divideAmount = (int) Math.round(marketBuyDto.getBuyPrice() * divideRate);

            EscrowDto escrowDto = EscrowDto.builder()
                    .account(escrow.getAccount())
                    .transSeq(12345) // TODO: 거래번호 생성 로직 필요
                    .userSeq(t.getUserSeq())
                    .transType(3)
                    .amount(divideAmount)
                    .build();

            escrowClient.escrowWithdrawal(escrowDto);

            Bank bank = bankRepository.findByUserSeqAndRole(t.getUserSeq(), "USER")
                    .orElseThrow(() -> new NotFound("해당 유저의 계좌 없음"));

            Long updatedDeposit = Math.round(bank.getDeposit() + (t.getAmount() * divideRate));
            bank.setDeposit(updatedDeposit.intValue());
            bankRepository.save(bank);
        }
    }
}
