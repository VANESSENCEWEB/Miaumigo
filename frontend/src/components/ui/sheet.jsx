import * as React from "react";
import * as SheetPrimitive from "@radix-ui/react-dialog";

import { X } from "lucide-react";

import { cn } from "@/lib/utils";

const Sheet = SheetPrimitive.Root;

const SheetTrigger = SheetPrimitive.Trigger;

const SheetClose = SheetPrimitive.Close;

const SheetPortal = SheetPrimitive.Portal;

const SheetOverlay = React.forwardRef(
  ({ className, ...props }, ref) => (
    <SheetPrimitive.Overlay
      ref={ref}
      className={cn(
        "fixed inset-0 z-50 bg-black/80",
        className
      )}
      {...props}
    />
  )
);

SheetOverlay.displayName = "SheetOverlay";

const SheetContent = React.forwardRef(
  (
    {
      side = "right",
      className,
      children,
      ...props
    },
    ref
  ) => (
    <SheetPortal>
      <SheetOverlay />

      <SheetPrimitive.Content
        ref={ref}
        className={cn(
          `
          fixed z-50 bg-white p-6 shadow-lg
          ${
            side === "right"
              ? "right-0 top-0 h-full w-3/4 sm:max-w-sm"
              : ""
          }
          ${
            side === "left"
              ? "left-0 top-0 h-full w-3/4 sm:max-w-sm"
              : ""
          }
          ${
            side === "top"
              ? "top-0 left-0 right-0"
              : ""
          }
          ${
            side === "bottom"
              ? "bottom-0 left-0 right-0"
              : ""
          }
          `,
          className
        )}
        {...props}
      >
        <SheetPrimitive.Close
          className="absolute right-4 top-4 rounded-sm opacity-70 hover:opacity-100"
        >
          <X className="h-4 w-4" />
        </SheetPrimitive.Close>

        {children}
      </SheetPrimitive.Content>
    </SheetPortal>
  )
);

SheetContent.displayName = "SheetContent";

const SheetHeader = ({
  className,
  ...props
}) => (
  <div
    className={cn(
      "flex flex-col space-y-2 text-center sm:text-left",
      className
    )}
    {...props}
  />
);

SheetHeader.displayName = "SheetHeader";

const SheetFooter = ({
  className,
  ...props
}) => (
  <div
    className={cn(
      "flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2",
      className
    )}
    {...props}
  />
);

SheetFooter.displayName = "SheetFooter";

const SheetTitle = React.forwardRef(
  ({ className, ...props }, ref) => (
    <SheetPrimitive.Title
      ref={ref}
      className={cn(
        "text-lg font-semibold",
        className
      )}
      {...props}
    />
  )
);

SheetTitle.displayName = "SheetTitle";

const SheetDescription = React.forwardRef(
  ({ className, ...props }, ref) => (
    <SheetPrimitive.Description
      ref={ref}
      className={cn(
        "text-sm text-gray-500",
        className
      )}
      {...props}
    />
  )
);

SheetDescription.displayName =
  "SheetDescription";

export {
  Sheet,
  SheetPortal,
  SheetOverlay,
  SheetTrigger,
  SheetClose,
  SheetContent,
  SheetHeader,
  SheetFooter,
  SheetTitle,
  SheetDescription,
};