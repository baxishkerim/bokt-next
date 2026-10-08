package az.bokt.common.util;

import org.springframework.data.domain.Page;

import java.util.List;

/** Универсальная обёртка для страничных ответов API. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <E, D> PageResponse<D> from(Page<E> page, java.util.function.Function<E, D> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
