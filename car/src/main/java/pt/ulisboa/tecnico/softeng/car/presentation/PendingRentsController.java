package pt.ulisboa.tecnico.softeng.car.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pt.ulisboa.tecnico.softeng.car.services.local.RentACarInterface;

@Controller
@RequestMapping(value = "/rentacars/rentacar/{code}/pendingrents")
public class PendingRentsController {
    private static final Logger logger = LoggerFactory.getLogger(VehiclesController.class);

    @RequestMapping(method = RequestMethod.GET)
    public String pendingRentsForm(Model model, @PathVariable String code) {
        logger.info("pendingRents");

        RentACarInterface rentACarInterface = new RentACarInterface();

        model.addAttribute("rentacar", rentACarInterface.getRentACarData(code));
        model.addAttribute("pendingRents", rentACarInterface.getPendingRents(code));
        return "pendingRentsView";
    }
}