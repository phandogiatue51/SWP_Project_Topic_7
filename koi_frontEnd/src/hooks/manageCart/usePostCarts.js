import { useMutation } from "@tanstack/react-query";
import { manageCartService } from "../../services/manageCartServices";

export const usePostCarts = () => {
    return useMutation({
        mutationFn: (payload) => {
            return manageCartService.postCart(payload)
        }
    })
}