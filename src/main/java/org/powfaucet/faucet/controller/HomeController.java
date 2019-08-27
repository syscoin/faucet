package org.powfaucet.faucet.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.powfaucet.faucet.service.RpcCommandGenerator;
import org.powfaucet.faucet.service.RpcService;
import org.powfaucet.faucet.util.Claim;
import org.powfaucet.faucet.util.RewardComputer;
import org.powfaucet.faucet.util.Settings;
import org.powfaucet.faucet.util.UserData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.powfaucet.faucet.util.RewardComputer.COIN_BD;

@Controller
public class HomeController {

    private final Object cacheLock = new Object();
    @GuardedBy("cacheLock")
    private final Cache<String, UserData> cache;
    private final ObjectMapper mapper;
    private final Settings settings;
    private final RpcService rpcService;
    private final RpcCommandGenerator rpcCommandGenerator;
    private final RewardComputer rewardComputer;

    public HomeController(
            ObjectMapper mapper,
            Settings settings,
            RpcService rpcService,
            RpcCommandGenerator rpcCommandGenerator,
            RewardComputer rewardComputer
    ) {
        this.mapper = mapper;
        this.settings = settings;
        this.rpcService = rpcService;
        this.rpcCommandGenerator = rpcCommandGenerator;
        this.rewardComputer = rewardComputer;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(settings.cacheMaxSize)
                .expireAfterWrite(settings.maxSessionTime, TimeUnit.SECONDS)
                .build();
    }

    @GetMapping("/")
    public String showAddressForm(
            Model model,
            @SuppressWarnings("unused") AddressForm addressForm
    ) {
        String errorCode = model.asMap().getOrDefault("errorCode", "0").toString();
        String[] parts = errorCode.split("\\-");
        model.addAttribute("errorCode", parts[0]);

        switch (parts[0]) {
            case "1":
                model.addAttribute("errorMessage", "The session has expired. Please try again.");
                break;
            case "2":
                model.addAttribute("errorMessage", "No special characters are allowed in " + settings.coinName + " address.");
                break;
            case "3":
                model.addAttribute("errorMessage", "Invalid " + settings.coinName + " address.");
                break;
            case "4":
                model.addAttribute("errorMessage", "Seed value is invalid.");
                break;
            case "5":
                model.addAttribute("errorMessage", "No reward. PoW is not sufficient.");
                break;
            case "6":
                if (parts.length < 2) {
                    model.addAttribute("errorMessage", "Your IP address received reward recently. Try again later.");
                } else {
                    model.addAttribute("errorMessage", "Your IP address received reward recently. Try again in " + parts[1] + " seconds.");
                }
                break;
            case "7":
                model.addAttribute("errorMessage", "Faucet backend is down for maintenance. Try again later.");
                break;
            case "8":
                model.addAttribute("errorMessage", "Address verification failed. Please report this problem.");
                break;
            case "9":
                model.addAttribute("errorMessage", "Internal error when checking target address. Please report this problem.");
                break;
            case "10":
                model.addAttribute("errorMessage", "Internal error when sending reward. Please report this problem.");
                break;
            case "11":
                model.addAttribute("errorMessage", "Web application error when sending reward. Please report this problem.");
                break;
            case "12":
                model.addAttribute("errorMessage", "Internal error when checking balance. Please report this problem.");
                break;
            case "13":
                model.addAttribute("errorMessage", "Faucet does not have enough funds at the moment. Please try again later.");
                break;
            case "14":
                model.addAttribute("errorMessage", "Reward was already collected.");
                break;
        }

        model.addAttribute("settings", settings);
        return "index";
    }

    @PostMapping(path = "/mine")
    public ModelAndView handleAddressFormSubmitted(
            Model model,
            @Valid AddressForm addressForm,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("settings", settings);

        if (bindingResult.hasErrors()) {
            return new ModelAndView("index", model.asMap());
        }

        // Check available balance
        try {
            String jsonResult = rpcService.requestSync(rpcCommandGenerator.getBalance());

            JsonNode jsonNode = mapper.readTree(jsonResult);
            System.out.println("Balance is: " + jsonResult);

            if (!jsonNode.get("error").isNull()) {
                System.out.println("Invalid target address: " + httpRequest.getRemoteAddr());
                redirectAttributes.addFlashAttribute("errorCode", "12");
                return new ModelAndView("redirect:/");
            }

            BigDecimal balanceInSat = new BigDecimal(jsonNode.get("result").asText()).multiply(COIN_BD);
            BigDecimal required = BigDecimal.valueOf(cache.size()).multiply(settings.maxRewardBD); // Heuristic

            if (balanceInSat.compareTo(required) < 0) {
                System.out.println("Balance is not sufficient; balance: " + balanceInSat + "; required: " + required.toPlainString());
                redirectAttributes.addFlashAttribute("errorCode", "13");
                return new ModelAndView("redirect:/");
            }
        } catch (CompletionException e) {
            // QT is not running
            redirectAttributes.addFlashAttribute("errorCode", "7");
            return new ModelAndView("redirect:/");
        } catch (Exception e) {
            // JSON is not correct or any other problem
            redirectAttributes.addFlashAttribute("errorCode", "8");
            return new ModelAndView("redirect:/");
        }

        // Check target address whether it is valid
        try {
            String jsonResult = rpcService.requestSync(rpcCommandGenerator.getAddressInfo(addressForm.getAddress()));
            JsonNode jsonNode = mapper.readTree(jsonResult);

            if (!jsonNode.get("error").isNull()) {
                System.out.println("Invalid target address: " + httpRequest.getRemoteAddr());
                redirectAttributes.addFlashAttribute("errorCode", "3");
                return new ModelAndView("redirect:/");
            }
        } catch (CompletionException e) {
            // QT is not running
            redirectAttributes.addFlashAttribute("errorCode", "7");
            return new ModelAndView("redirect:/");
        } catch (Exception e) {
            // JSON is not correct or any other problem
            redirectAttributes.addFlashAttribute("errorCode", "8");
            return new ModelAndView("redirect:/");
        }

        synchronized (cacheLock) {
            UserData userData = getUserData(httpRequest);

            if (userData != null) {
                if (!userData.getBestHash().isEmpty()) {
                    System.out.println("IP address '" + userData.ip + "' tried to start another session too soon after last claim.");
                    long seconds = Duration.between(Instant.now(), userData.created.plusSeconds(settings.maxSessionTime)).toSeconds();
                    redirectAttributes.addFlashAttribute("errorCode", "6-" + seconds);
                    return new ModelAndView("redirect:/");
                }
            }

            // Re-write old data possibly
            userData = generateNewUserDataToCache(httpRequest);
            userData.setTargetAddress(addressForm.getAddress());

            ClaimForm claimForm = new ClaimForm();
            claimForm.setSeed(userData.seed);

            model.addAttribute("claimForm", claimForm);
            model.addAttribute("userData", userData);
        }

        return new ModelAndView("mine", model.asMap());
    }

    @PostMapping(path = "/claim")
    public RedirectView handleClaimFormSubmitted(
            Model model,
            RedirectAttributes redirectAttributes,
            @Valid ClaimForm claimForm,
            BindingResult bindingResult,
            HttpServletRequest httpRequest
    ) {
        model.addAttribute("settings", settings);

        if (bindingResult.hasErrors()) {
            return new RedirectView("/", true);
        }

        BigInteger rewardSat;
        BigDecimal rewardInCoin;
        String targetAddress;

        synchronized (cacheLock) {
            UserData userData = getUserData(httpRequest);

            if (userData == null) {
                System.out.println("No user data found for: " + httpRequest.getRemoteAddr());
                redirectAttributes.addFlashAttribute("errorCode", "1");
                return new RedirectView("/", true);
            }

            if (!claimForm.getSeed().equals(userData.seed)) {
                System.out.println("Seed changed!: " + claimForm.getSeed() + " vs " + userData);
                redirectAttributes.addFlashAttribute("errorCode", "4");
                return new RedirectView("/", true);
            }

            if (userData.getBestHash().isEmpty()) {
                userData.setBestHash(claimForm.getBestHash());
            } else {
                System.out.println("Best hash was already set: " + userData);
                redirectAttributes.addFlashAttribute("errorCode", "14");
                return new RedirectView("/", true);
            }

            BigInteger Ri = new BigInteger(userData.getBestHash(), 16);
            rewardSat = rewardComputer.compute(Ri, BigInteger.valueOf(settings.hashesPerCoin), settings.getMaxHash());

            if (rewardSat.compareTo(settings.minReward) < 0) {
                System.out.println("PoW is not sufficient");
                redirectAttributes.addFlashAttribute("errorCode", "5");
                return new RedirectView("/", true);
            }

            if (rewardSat.compareTo(settings.maxReward) > 0) {
                System.out.println("Reward exceeded maxReward, setting to maxReward");
                rewardSat = settings.maxReward;
            }

            rewardInCoin = new BigDecimal(rewardSat).divide(COIN_BD, 8, RoundingMode.FLOOR);
            System.out.println("UserData: " + userData + "; reward: " + rewardInCoin.toPlainString());

            targetAddress = userData.getTargetAddress();
        }

        String jsonResult;
        String txid;

        try {
            jsonResult = rpcService.requestSync(rpcCommandGenerator.sendToAddress(targetAddress, rewardInCoin));
            System.out.println("Result: " + jsonResult);

            JsonNode jsonNode = mapper.readTree(jsonResult);
            JsonNode errorNode = jsonNode.get("error");

            if (!errorNode.isNull()) {
                System.out.println("Invalid target address: " + httpRequest.getRemoteAddr());
                redirectAttributes.addFlashAttribute("errorCode", "10");
                return new RedirectView("/", true);
            }

            txid = jsonNode.get("result").asText();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorCode", "11");
            return new RedirectView("/", true);
        }

        Claim claim = new Claim(
                new BigDecimal(rewardSat).divide(COIN_BD, 8, RoundingMode.FLOOR).toPlainString(),
                targetAddress,
                txid
        );
        redirectAttributes.addFlashAttribute("claim", claim);

        return new RedirectView("/", true);
    }

    @Nullable
    @GuardedBy("cacheLock")
    private UserData getUserData(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return cache.getIfPresent(ipAddress);
    }

    @GuardedBy("cacheLock")
    private UserData generateNewUserDataToCache(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String seed = UUID.randomUUID().toString().replace("-", "");
        UserData userData = new UserData(Instant.now(), seed, ipAddress);

        cache.put(ipAddress, userData);
        return userData;
    }
}