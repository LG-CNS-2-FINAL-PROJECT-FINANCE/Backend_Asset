package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.common.exception.BadParameter;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.component.ExternalPriceApi;
import com.ddiring.backend_asset.dto.CreateWalletDto;
import com.ddiring.backend_asset.dto.CreateWalletAddressDto; // WalletCreationResponseDto 대신 CreateWalletAddressDto 사용
import com.ddiring.backend_asset.dto.MarketSellDto;
import com.ddiring.backend_asset.dto.WalletTokenInfoDto;
import com.ddiring.backend_asset.entitiy.Bank;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.repository.TokenRepository;
import com.ddiring.backend_asset.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TokenRepository tokenRepository; // TokenRepository 주입
    private final ExternalPriceApi externalPriceApi; // 토큰 가격 조회용 외부 API

    // Web3j 인스턴스 (이더리움 노드 URL에 맞게 설정)
    // "YOUR_INFURA_PROJECT_ID"를 실제 프로젝트 ID로 교체해야 합니다.
    private final Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/b0a9ea641fa84233a73393f8bd79b3d2"));

    // 특정 userSeq에 해당하는 지갑을 찾는 메소드 (예외 처리 포함)
    @Transactional
    public Wallet findByUserSeq(String userSeq) {
        return walletRepository.findByUserSeq(userSeq)
                .orElseThrow(() -> new com.ddiring.backend_asset.common.exception.NotFound("지갑을 찾을 수 없습니다."));
    }

    // 새 이더리움 지갑을 생성하고 주소와 개인 키를 반환하는 메소드
    @Transactional
    public CreateWalletAddressDto createWalletAndReturnKeys(String userSeq) {

        Optional<Wallet> userWallet = walletRepository.findByUserSeq(userSeq);
        if (userWallet.isPresent()) {
            throw new com.ddiring.backend_asset.common.exception.BadParameter("지갑 있");
        }
        try {
            // 이미 지갑이 존재하는지 확인

            // 1. 새로운 이더리움 키페어(개인 키, 공개 키) 생성
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();

            // 2. 개인 키를 사용하여 Credentials 객체 생성
            Credentials credentials = Credentials.create(ecKeyPair);

            // 3. 지갑 주소와 개인 키 추출
            String walletAddress = credentials.getAddress();
            String privateKey = Numeric.toHexStringWithPrefix(credentials.getEcKeyPair().getPrivateKey());

            // 4. 지갑 주소를 DB에 저장 (개인 키는 저장하지 않음)
            Wallet wallet = Wallet.builder()
                    .userSeq(userSeq)
                    .walletAddress(walletAddress)
                    .build();

            walletRepository.save(wallet);

            // 5. 지갑 주소와 개인 키를 DTO에 담아 반환
            return new CreateWalletAddressDto(walletAddress, privateKey);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("지갑 생성에 실패했습니다.", e);
        }
    }

    @Transactional
    public String getWalletAddress(String userSeq) {
        Optional<Wallet> userWallet = walletRepository.findByUserSeq(userSeq);
        if (!userWallet.isPresent()) {
            throw new BadParameter("지갑 주소가 없습니다");
        }
        return userWallet.get().getWalletAddress();
    }
}
